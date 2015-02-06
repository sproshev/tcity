/*
 * Copyright 2014 Semyon Proshev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tcity.android.ui.build.artifacts;

import android.app.DownloadManager;
import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Common;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.ui.build.BuildActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class BuildArtifactsFragment
        extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener, BuildArtifactListener {

    @NotNull
    public static final String TAG = BuildArtifactsFragment.class.getName();

    @NotNull
    private String myBuildId;

    @NotNull
    private BuildArtifactsAdapter myAdapter;

    @NotNull
    private LinkedList<String> myPathStack;

    @NotNull
    private BuildArtifactsCache myCache;

    @NotNull
    private RestClient myClient;

    @Nullable
    private BuildArtifactsTask myTask;

    @NotNull
    private SwipeRefreshLayout myLayout;

    // LIFECYCLE - Begin

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myBuildId = getArguments().getString(BuildActivity.ID_INTENT_KEY);
        myAdapter = new BuildArtifactsAdapter(getActivity(), this);

        myPathStack = new LinkedList<>();
        myCache = new BuildArtifactsCache();

        myClient = new RestClient(new Preferences(getActivity()));

        calculateNewTask();

        setRetainInstance(true);
    }

    @NotNull
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.overview_ui, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.overview_srlayout);
        myLayout.setColorSchemeResources(R.color.green, R.color.red);
        myLayout.setOnRefreshListener(this);

        getListView().setAdapter(myAdapter);
        getListView().setSelector(android.R.color.transparent);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (myTask != null) {
            if (myTask.getStatus() == AsyncTask.Status.PENDING) {
                if (Common.isNetworkAvailable(getActivity())) {
                    onRefresh();
                } else {
                    ((TextView) getListView().getEmptyView()).setText(R.string.network_is_unavailable);
                }
            } else if (myTask.getStatus() == AsyncTask.Status.RUNNING) {
                if (myTask.isCancelled()) {
                    onRefreshFinished();
                } else {
                    onRefreshRunning();
                }
            } else if (myTask.getStatus() == AsyncTask.Status.FINISHED) {
                onRefreshFinished();
            }

            myTask.setFragment(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (myTask != null) {
            myTask.setFragment(null);
        }
    }

    // LIFECYCLE - End

    @Override
    public void onRefresh() {
        if (myTask == null || myTask.getStatus() == AsyncTask.Status.FINISHED || myTask.isCancelled()) {
            calculateNewTask();
        }

        if (myTask.getStatus() != AsyncTask.Status.RUNNING) {
            myTask.execute();
        }
    }

    @Override
    public void onDownloadClick(@NotNull BuildArtifact artifact) {
        //noinspection ResultOfMethodCallIgnored
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(calculateArtifactRequest(artifact));
    }

    @Override
    public void onDescriptionClick(@NotNull BuildArtifact artifact) {
        if (artifact.childrenHref == null) {
            throw new IllegalArgumentException("Build artifact hasn't children");
        }

        if (myTask != null && myTask.getStatus() == AsyncTask.Status.RUNNING) {
            myTask.setFragment(null);
            myTask.cancel(true);
        }

        myPathStack.addLast(artifact.childrenHref);

        if (myCache.containsKey(artifact.childrenHref)) {
            myTask = null;

            myAdapter.setData(
                    myCache.get(artifact.childrenHref)
            );

            myAdapter.notifyDataSetChanged();
        } else {
            calculateNewTask();

            myTask.execute();
        }
    }

    public boolean onBackPressed() {
        if (myPathStack.isEmpty()) {
            return false;
        }

        if (myTask != null && myTask.getStatus() == AsyncTask.Status.RUNNING) {
            myTask.setFragment(null);
            myTask.cancel(true);
        }

        myPathStack.pollLast();

        if (myCache.containsKey(myPathStack.peekLast())) {
            myTask = null;

            myAdapter.setData(
                    myCache.get(myPathStack.peekLast())
            );

            myAdapter.notifyDataSetChanged();
        } else {
            calculateNewTask();

            myTask.execute();
        }

        return true;
    }

    void onRefreshRunning() {
        setRefreshing(true);
    }

    void onRefreshFinished() {
        setRefreshing(false);

        if (myTask != null) {
            //noinspection ThrowableResultOfMethodCallIgnored
            Exception e = myTask.getException();

            if (e != null) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            List<BuildArtifact> result = myTask.getResult();

            if (result != null) {
                myCache.put(myPathStack.peekLast(), result);

                myAdapter.setData(result);
                myAdapter.notifyDataSetChanged();
            }
        }
    }

    private void calculateNewTask() {
        myTask = new BuildArtifactsTask(
                myBuildId,
                myPathStack.peekLast(),
                myClient
        );
        myTask.setFragment(this);
    }

    @NotNull
    private DownloadManager.Request calculateArtifactRequest(@NotNull BuildArtifact artifact) {
        if (artifact.contentHref == null) {
            throw new IllegalStateException("Build artifact hasn't content");
        }

        Preferences preferences = new Preferences(getActivity());

        Uri src = Uri.parse(preferences.getUrl() + artifact.contentHref);
        String dest = artifact.name;

        DownloadManager.Request request = new DownloadManager.Request(src);

        request.addRequestHeader("Authorization", "Basic " + preferences.getAuth());
        request.setTitle(dest);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                dest
        );

        return request;
    }

    private void setRefreshing(boolean refreshing) {
        Common.setRefreshing(
                getActivity(),
                myLayout,
                (TextView) getListView().getEmptyView(),
                refreshing
        );
    }

    private static class BuildArtifactsCache extends LinkedHashMap<String, List<BuildArtifact>> {

        private static final int SIZE = 5;

        public BuildArtifactsCache() {
            super(SIZE);
        }

        @Override
        protected boolean removeEldestEntry(@Nullable Entry<String, List<BuildArtifact>> eldest) {
            return size() > SIZE;
        }
    }
}
