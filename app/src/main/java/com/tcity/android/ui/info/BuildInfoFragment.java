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

package com.tcity.android.ui.info;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Common;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BuildInfoFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    private String myBuildId;

    @NotNull
    private ArrayAdapter<String> myAdapter;

    @NotNull
    private BuildInfoTask myTask;

    @NotNull
    private SwipeRefreshLayout myLayout;

    // LIFECYCLE - Begin


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myBuildId = getArguments().getString(BuildHostActivity.ID_INTENT_KEY);
        myAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        myTask = new BuildInfoTask(myBuildId, new RestClient(new Preferences(getActivity())));
        myTask.setFragment(this);

        setRetainInstance(true);
    }

    @Nullable
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
        getListView().setDivider(null);
        getListView().setSelector(android.R.color.transparent);

        if (myTask.getStatus() == AsyncTask.Status.PENDING) {
            if (Common.isNetworkAvailable(getActivity())) {
                onRefresh();
            } else {
                ((TextView) getListView().getEmptyView()).setText(R.string.network_is_unavailable);
            }
        } else if (myTask.getStatus() == AsyncTask.Status.RUNNING) {
            onRefreshStarted();
        } else if (myTask.getStatus() == AsyncTask.Status.FINISHED) {
            onRefreshFinished();
        }

        myTask.setFragment(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        myTask.setFragment(null);
    }

    // LIFECYCLE - End

    @Override
    public void onRefresh() {
        if (myTask.getStatus() == AsyncTask.Status.FINISHED) {
            myTask = new BuildInfoTask(
                    myBuildId,
                    new RestClient(
                            new Preferences(getActivity())
                    )
            );

            myTask.setFragment(this);
        }

        if (myTask.getStatus() != AsyncTask.Status.RUNNING) {
            myTask.execute();
        }
    }

    void onRefreshStarted() {
        setRefreshing(true);
    }

    void onRefreshFinished() {
        setRefreshing(false);

        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = myTask.getException();

        if (e != null) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        BuildInfoData result = myTask.getResult();

        if (result != null) {
            updateAdapter(result);
        }
    }

    private void setRefreshing(final boolean refreshing) {
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (myLayout.isRefreshing() ^ refreshing) {
                            myLayout.setRefreshing(refreshing);

                            TextView emptyView = (TextView) getListView().getEmptyView();

                            if (refreshing) {
                                emptyView.setText(R.string.loading);
                            } else {
                                if (Common.isNetworkAvailable(getActivity())) {
                                    emptyView.setText(R.string.empty);
                                } else {
                                    emptyView.setText(R.string.network_is_unavailable);
                                }
                            }
                        }
                    }
                }, 500
        );  // https://code.google.com/p/android/issues/detail?id=77712
    }

    private void updateAdapter(@NotNull BuildInfoData result) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

        myAdapter.clear();

        if (result.result != null) {
            myAdapter.add("Status: " + result.result);
        }

        if (result.branch != null) {
            myAdapter.add("Branch: " + result.branch);
        }

        if (result.waitReason != null) {
            myAdapter.add("Wait Reason: " + result.waitReason);
        }

        if (result.queued != null) {
            myAdapter.add("Queued: " + dateFormat.format(result.queued));
        }

        if (result.started != null) {
            myAdapter.add("Started: " + dateFormat.format(result.started));
        }

        if (result.finished != null) {
            myAdapter.add("Finished: " + dateFormat.format(result.finished));
        }

        if (result.agent != null) {
            myAdapter.add("Agent: " + result.agent);
        }

        myAdapter.notifyDataSetChanged();
    }
}
