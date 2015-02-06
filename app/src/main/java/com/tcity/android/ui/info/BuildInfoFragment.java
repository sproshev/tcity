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

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BuildInfoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    public static final String TAG = BuildInfoFragment.class.getName();

    @NotNull
    private String myBuildId;

    @NotNull
    private RestClient myClient;

    @NotNull
    private BuildInfoTask myTask;

    @NotNull
    private SwipeRefreshLayout myLayout;

    // LIFECYCLE - Begin

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myBuildId = getArguments().getString(BuildHostActivity.ID_INTENT_KEY);

        myClient = new RestClient(new Preferences(getActivity()));

        calculateNewTask();

        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.build_info_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myLayout = (SwipeRefreshLayout) getView().findViewById(R.id.build_info_srlayout);
        myLayout.setColorSchemeResources(R.color.green, R.color.red);
        myLayout.setOnRefreshListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (myTask.getStatus() == AsyncTask.Status.PENDING) {
            if (Common.isNetworkAvailable(getActivity())) {
                onRefresh();
            } else {
                TextView emptyView = (TextView) getView().findViewById(android.R.id.empty);
                emptyView.setText(R.string.network_is_unavailable);
            }
        } else if (myTask.getStatus() == AsyncTask.Status.RUNNING) {
            onRefreshStarted();
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

    @NotNull
    @Override
    public View getView() {
        //noinspection ConstantConditions
        return super.getView();
    }

    @Override
    public void onRefresh() {
        if (myTask.getStatus() == AsyncTask.Status.FINISHED) {
            calculateNewTask();
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
            updateView(result);
        }
    }

    private void calculateNewTask() {
        myTask = new BuildInfoTask(myBuildId, myClient);
        myTask.setFragment(this);
    }

    private void setRefreshing(final boolean refreshing) {
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (myLayout.isRefreshing() ^ refreshing) {
                            myLayout.setRefreshing(refreshing);

                            TextView emptyView = (TextView) getView().findViewById(android.R.id.empty);

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

    private void updateView(@NotNull BuildInfoData result) {
        updateResultView(result);
        updateBranchView(result);
        updateWaitReasonView(result);

        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

        updateQueuedView(result, dateFormat);
        updateStartedView(result, dateFormat);
        updateFinishedView(result, dateFormat);

        updateAgentView(result);

        updateVisibility(result);
    }

    private void updateResultView(@NotNull BuildInfoData data) {
        View row = getView().findViewById(R.id.build_info_result_row);

        if (data.result != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_result);
            view.setText(data.result);
        } else {
            row.setVisibility(View.GONE);
        }

        if (data.status != null) {
            row.setBackgroundColor(Common.loadBackgroundColor(data.status, getActivity()));
            row.getBackground().setAlpha(50);
        }
    }

    private void updateBranchView(@NotNull BuildInfoData data) {
        View row = getView().findViewById(R.id.build_info_branch_row);

        if (data.branch != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_branch);
            view.setText(data.branch);

            if (data.isBranchDefault) {
                view.setTypeface(null, Typeface.BOLD);
            } else {
                view.setTypeface(null, Typeface.NORMAL);
            }
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateWaitReasonView(@NotNull BuildInfoData data) {
        View row = getView().findViewById(R.id.build_info_wait_reason_row);

        if (data.waitReason != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_wait_reason);
            view.setText(data.waitReason);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateQueuedView(@NotNull BuildInfoData data, @NotNull DateFormat dateFormat) {
        View row = getView().findViewById(R.id.build_info_queued_row);

        if (data.queued != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_queued);
            view.setText(dateFormat.format(data.queued));
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateStartedView(@NotNull BuildInfoData data, @NotNull DateFormat dateFormat) {
        View row = getView().findViewById(R.id.build_info_started_row);

        if (data.started != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_started);
            view.setText(dateFormat.format(data.started));
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateFinishedView(@NotNull BuildInfoData data, @NotNull DateFormat dateFormat) {
        View row = getView().findViewById(R.id.build_info_finished_row);

        if (data.finished != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_finished);
            view.setText(dateFormat.format(data.finished));
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateAgentView(@NotNull BuildInfoData data) {
        View row = getView().findViewById(R.id.build_info_agent_row);

        if (data.agent != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_agent);
            view.setText(data.agent);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateVisibility(@NotNull BuildInfoData result) {
        if (result.result != null ||
                result.branch != null ||
                result.waitReason != null ||
                result.queued != null ||
                result.started != null ||
                result.finished != null ||
                result.agent != null) {
            getView().findViewById(android.R.id.empty).setVisibility(View.GONE);
            getView().findViewById(R.id.build_info_tablelayout).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.build_info_tablelayout).setVisibility(View.GONE);
        }
    }
}
