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

import android.app.ActionBar;
import android.app.ListActivity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.tcity.android.R;
import com.tcity.android.app.Application;

import org.jetbrains.annotations.NotNull;

public class ProjectOverviewActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private ProjectOverviewEngine myEngine;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.overview);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(R.string.overview);
        }

        myEngine = calculateEngine();
        myEngine.setActivity(this);
        setListAdapter(myEngine.getAdapter());

        myLayout = (SwipeRefreshLayout) findViewById(R.id.overview_layout);
        myLayout.setColorSchemeColors(R.color.green_status, R.color.red_status);

        myEngine.refresh();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object onRetainNonConfigurationInstance() {
        return myEngine;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        myEngine.setActivity(null);
    }

    // LIFECYCLE - End


    @Override
    public void onRefresh() {
        myEngine.refresh();
    }

    void setRefreshing(boolean refreshing) {
        myLayout.setRefreshing(refreshing);
    }

    void onImageClick(@NotNull String id) {

    }

    void onNameClick(@NotNull String id) {

    }

    void onOptionsClick(@NotNull String id, @NotNull View anchor) {

    }

    @NotNull
    private ProjectOverviewEngine calculateEngine() {
        @SuppressWarnings("deprecation")
        ProjectOverviewEngine result = (ProjectOverviewEngine) getLastNonConfigurationInstance();

        if (result == null) {
            result = new ProjectOverviewEngine(
                    getApplicationContext(),
                    ((Application) getApplication()).getDB(),
                    getListView()
            );
        }

        return result;
    }
}
