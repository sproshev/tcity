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

import java.util.Map;

public class BuildTestsFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    private String myBuildId;

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private ArrayAdapter<String> myAdapter;

    @Nullable
    private BuildTestsTask myTask;

    // LIFECYCLE - Begin

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

        myBuildId = getArguments().getString(BuildHostActivity.ID_INTENT_KEY);

        myLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.overview_srlayout);
        myLayout.setColorSchemeResources(R.color.green, R.color.red);
        myLayout.setOnRefreshListener(this);

        myAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        getListView().setAdapter(myAdapter);
        getListView().setDivider(null);
        getListView().setSelector(android.R.color.transparent);

        if (Common.isNetworkAvailable(getActivity())) {
            onRefresh();
        } else {
            ((TextView) getListView().getEmptyView()).setText(R.string.network_is_unavailable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //noinspection ConstantConditions
        myBuildId = null;

        //noinspection ConstantConditions
        myLayout = null;

        if (myTask != null) {
            myTask.cancel(true);
            myTask = null;
        }
    }

    // LIFECYCLE - End

    @Override
    public void onRefresh() {
        if (myTask == null || myTask.getStatus() == AsyncTask.Status.FINISHED) {
            myTask = new BuildTestsTask(
                    myBuildId,
                    this,
                    new RestClient(
                            new Preferences(getActivity())
                    )
            );
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

        if (myTask == null) {
            return;
        }

        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = myTask.getException();
        Map<String, String> result = myTask.getResult();

        if (e != null) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        } else if (result != null) {
            myAdapter.clear();

            for (Map.Entry<String, String> kv : result.entrySet()) {
                myAdapter.add(kv.getKey() + ": " + kv.getValue());
            }

            myAdapter.notifyDataSetChanged();
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
}
