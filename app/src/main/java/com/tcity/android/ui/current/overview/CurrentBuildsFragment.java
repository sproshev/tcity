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

package com.tcity.android.ui.current.overview;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.Common;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.db.DB;
import com.tcity.android.obj.QueuedBuild;
import com.tcity.android.obj.RunningBuild;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CurrentBuildsFragment
        extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener, RunningBuildListener, QueuedBuildListener {

    @NotNull
    public static final String TAG = CurrentBuildsFragment.class.getName();

    @NotNull
    private TextView myRunningBuildsHeader;

    @NotNull
    private RunningBuildAdapter myRunningBuildAdapter;

    @NotNull
    private TextView myQueuedBuildsHeader;

    @NotNull
    private QueuedBuildAdapter myQueuedBuildAdapter;

    @NotNull
    private MergeAdapter myAdapter;

    @NotNull
    private RestClient myClient;

    @NotNull
    private CurrentBuildsTask myTask;

    @NotNull
    private SwipeRefreshLayout myLayout;

    // LIFECYCLE - Begin

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        myRunningBuildsHeader = (TextView) inflater.inflate(R.layout.overview_separator, null, false); // TODO
        myRunningBuildsHeader.setText("Running builds");

        myQueuedBuildsHeader = (TextView) inflater.inflate(R.layout.overview_separator, null, false); // TODO
        myQueuedBuildsHeader.setText("Queued builds");

        DB db = ((Application) getActivity().getApplication()).getDb();

        myRunningBuildAdapter = new RunningBuildAdapter(
                getActivity(),
                this,
                db
        );

        myQueuedBuildAdapter = new QueuedBuildAdapter(
                getActivity(),
                this,
                db
        );

        myAdapter = new MergeAdapter();

        myAdapter.addView(myRunningBuildsHeader, false);
        myAdapter.addAdapter(myRunningBuildAdapter);

        myAdapter.addView(myQueuedBuildsHeader, false);
        myAdapter.addAdapter(myQueuedBuildAdapter);

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

        if (myTask.getStatus() == AsyncTask.Status.PENDING) {
            if (Common.isNetworkAvailable(getActivity())) {
                onRefresh();
            } else {
                ((TextView) getListView().getEmptyView()).setText(R.string.network_is_unavailable);
            }
        } else if (myTask.getStatus() == AsyncTask.Status.RUNNING) {
            onRefreshRunning();
        } else if (myTask.getStatus() == AsyncTask.Status.FINISHED) {
            onRefreshFinished();
        }

        myTask.setFragment(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        myTask.setFragment(null);
    }

    // LIFECYCLE - End

    @Override
    public void onRefresh() {
        if (myTask.getStatus() == AsyncTask.Status.FINISHED) {
            calculateNewTask();
        }

        if (myTask.getStatus() != AsyncTask.Status.RUNNING) {
            myTask.execute();
        }
    }

    @Override
    public void onDescriptionClick(@NotNull String id) {
        // TODO
    }

    @Override
    public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
        // TODO
    }

    void onRefreshRunning() {
        setRefreshing(true);
    }

    void onRefreshFinished() {
        setRefreshing(false);

        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = myTask.getException();

        if (e != null) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        List<RunningBuild> runningBuilds = myTask.getRunningBuilds();
        List<QueuedBuild> queuedBuilds = myTask.getQueuedBuilds();

        if (runningBuilds != null) {
            myRunningBuildAdapter.setData(runningBuilds);

            myAdapter.setActive(myRunningBuildsHeader, !runningBuilds.isEmpty());
        }

        if (queuedBuilds != null) {
            myQueuedBuildAdapter.setData(queuedBuilds);

            myAdapter.setActive(myQueuedBuildsHeader, !queuedBuilds.isEmpty());
        }

        if (runningBuilds != null || queuedBuilds != null) {
            myAdapter.notifyDataSetChanged();
        }
    }

    private void calculateNewTask() {
        myTask = new CurrentBuildsTask(
                myClient,
                ((Application) getActivity().getApplication()).getDb()
        );
        myTask.setFragment(this);
    }

    private void setRefreshing(boolean refreshing) {
        Common.setRefreshing(
                getActivity(),
                myLayout,
                (TextView) getListView().getEmptyView(),
                refreshing
        );
    }
}
