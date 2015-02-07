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

package com.tcity.android.ui.project.overview;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.Common;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.web.WebLocator;
import com.tcity.android.obj.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RootOverviewFragment
        extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener, ProjectOverviewListener {

    @NotNull
    public static final String TAG = RootOverviewFragment.class.getName();

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private ProjectOverviewEngine myEngine;

    // LIFECYCLE - Begin

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

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

        if (myEngine == null) {
            myEngine = new ProjectOverviewEngine(
                    Project.ROOT_PROJECT_ID,
                    getActivity(),
                    ((Application) getActivity().getApplication()).getDb(),
                    getListView()
            );
        }

        getListView().setAdapter(myEngine.getAdapter());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Common.isNetworkAvailable(getActivity())) {
            myEngine.refresh(false);
        } else {
            ((TextView) getListView().getEmptyView()).setText(R.string.network_is_unavailable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (myEngine.isRefreshing()) {
            onRefreshRunning();
        } else {
            onRefreshException();
            onRefreshFinished();
        }

        myEngine.setListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        myEngine.setListener(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        myEngine.close();

        //noinspection ConstantConditions
        myEngine = null;

        //noinspection ConstantConditions
        myLayout = null;
    }

    // LIFECYCLE - End

    @Override
    public void onRefresh() {
        myEngine.refresh(true);
    }

    @Override
    public void onRefreshRunning() {
        setRefreshing(true);
    }

    @Override
    public void onRefreshFinished() {
        setRefreshing(false);
    }

    @Override
    public void onRefreshException() {
        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = myEngine.getException();

        if (e != null) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();

            myEngine.resetException();
        }
    }

    @Override
    public void projectImageClick(@NotNull String id) {
        myEngine.projectImageClick(id);
    }

    @Override
    public void projectDescriptionClick(@NotNull String id) {
        Intent intent = new Intent(getActivity(), ProjectOverviewActivity.class);
        intent.putExtra(ProjectOverviewActivity.ID_INTENT_KEY, id);

        startActivity(intent);
    }

    @Override
    public void projectOptionsClick(@NotNull String id, @NotNull View anchor) {
        Activity activity = getActivity();

        PopupMenu menu = new PopupMenu(activity, anchor);

        menu.inflate(R.menu.menu_concept);

        menu.setOnMenuItemClickListener(
                new Common.PopupMenuListener(
                        activity,
                        WebLocator.getProjectUrl(id, new Preferences(activity))
                )
        );

        menu.show();
    }

    @Override
    public void buildConfigurationImageClick(@NotNull String id) {
    }

    @Override
    public void buildConfigurationDescriptionClick(@NotNull String id) {
    }

    @Override
    public void buildConfigurationOptionsClick(@NotNull String id, @NotNull View anchor) {
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
