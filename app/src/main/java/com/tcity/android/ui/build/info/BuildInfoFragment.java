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

package com.tcity.android.ui.build.info;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.tcity.android.obj.BuildInfo;
import com.tcity.android.ui.build.BuildActivity;

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

        myBuildId = getArguments().getString(BuildActivity.ID_INTENT_KEY);

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

        BuildInfo result = myTask.getResult();

        if (result != null) {
            updateView(result);
        }
    }

    private void calculateNewTask() {
        myTask = new BuildInfoTask(myBuildId, myClient);
        myTask.setFragment(this);
    }

    private void setRefreshing(boolean refreshing) {
        Common.setRefreshing(
                getActivity(),
                myLayout,
                (TextView) getView().findViewById(android.R.id.empty),
                refreshing
        );
    }

    private void updateView(@NotNull BuildInfo info) {
        updateResultView(info);
        updateBranchView(info);
        updateWaitReasonView(info);

        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

        updateQueuedView(info, dateFormat);
        updateStartedView(info, dateFormat);
        updateFinishedView(info, dateFormat);

        updateAgentView(info);

        updateVisibility(info);
    }

    private void updateResultView(@NotNull BuildInfo info) {
        View row = getView().findViewById(R.id.build_info_result_row);

        if (info.result != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_result);
            view.setText(info.result);
        } else {
            row.setVisibility(View.GONE);
        }

        if (info.status != null) {
            row.setBackgroundColor(Common.loadBackgroundColor(info.status, getActivity()));
            row.getBackground().setAlpha(50);
        }
    }

    private void updateBranchView(@NotNull BuildInfo info) {
        View row = getView().findViewById(R.id.build_info_branch_row);

        if (info.branch != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_branch);
            view.setText(info.branch);

            if (info.isBranchDefault) {
                view.setTypeface(null, Typeface.BOLD);
            } else {
                view.setTypeface(null, Typeface.NORMAL);
            }
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateWaitReasonView(@NotNull BuildInfo info) {
        View row = getView().findViewById(R.id.build_info_wait_reason_row);

        if (info.waitReason != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_wait_reason);
            view.setText(info.waitReason);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateQueuedView(@NotNull BuildInfo info, @NotNull DateFormat dateFormat) {
        View row = getView().findViewById(R.id.build_info_queued_row);

        if (info.queued != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_queued);
            view.setText(dateFormat.format(info.queued));
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateStartedView(@NotNull BuildInfo info, @NotNull DateFormat dateFormat) {
        View row = getView().findViewById(R.id.build_info_started_row);

        if (info.started != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_started);
            view.setText(dateFormat.format(info.started));
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateFinishedView(@NotNull BuildInfo info, @NotNull DateFormat dateFormat) {
        View row = getView().findViewById(R.id.build_info_finished_row);

        if (info.finished != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_finished);
            view.setText(dateFormat.format(info.finished));
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateAgentView(@NotNull BuildInfo info) {
        View row = getView().findViewById(R.id.build_info_agent_row);

        if (info.agent != null) {
            row.setVisibility(View.VISIBLE);

            TextView view = (TextView) row.findViewById(R.id.build_info_agent);
            view.setText(info.agent);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void updateVisibility(@NotNull BuildInfo info) {
        if (info.result != null ||
                info.branch != null ||
                info.waitReason != null ||
                info.queued != null ||
                info.started != null ||
                info.finished != null ||
                info.agent != null) {
            getView().findViewById(android.R.id.empty).setVisibility(View.GONE);
            getView().findViewById(R.id.build_info_tablelayout).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.build_info_tablelayout).setVisibility(View.GONE);
        }
    }
}
