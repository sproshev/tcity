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

package com.tcity.android.ui.overview.buildconfiguration;

import android.app.ActionBar;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.DB;
import com.tcity.android.db.DbPackage;
import com.tcity.android.db.Schema;

import org.jetbrains.annotations.NotNull;

public class BuildConfigurationOverviewActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    public static final String INTENT_KEY = "BUILD_CONFIGURATION_ID";

    @NotNull
    private String myBuildConfigurationId;

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private BuildConfigurationOverviewEngine myEngine;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myBuildConfigurationId = getIntent().getStringExtra(INTENT_KEY);

        setContentView(R.layout.overview);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(calculateTitle());
        }

        myLayout = (SwipeRefreshLayout) findViewById(R.id.overview_layout);
        myLayout.setColorSchemeResources(R.color.green_status, R.color.red_status);
        myLayout.setOnRefreshListener(this);

        myEngine = calculateEngine();
        myEngine.setActivity(this);
        setListAdapter(myEngine.getAdapter());

        Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        myEngine.refresh();
                    }
                },
                1000
        ); // https://code.google.com/p/android/issues/detail?id=77712
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
        if (myLayout.isRefreshing() ^ refreshing) {
            myLayout.setRefreshing(refreshing);

            TextView emptyView = (TextView) getListView().getEmptyView();

            if (refreshing) {
                emptyView.setText(R.string.loading);
            } else {
                emptyView.setText(R.string.empty);
            }
        }
    }

    @NotNull
    private String calculateTitle() {
        DB db = ((Application) getApplication()).getDB();

        Cursor cursor = db.query(
                Schema.BUILD_CONFIGURATION,
                new String[]{Schema.NAME_COLUMN},
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{myBuildConfigurationId},
                null, null, null, null
        );

        cursor.moveToNext();

        String result = DbPackage.getName(cursor);

        cursor.close();

        return result;
    }

    @NotNull
    private BuildConfigurationOverviewEngine calculateEngine() {
        @SuppressWarnings("deprecation")
        BuildConfigurationOverviewEngine result = (BuildConfigurationOverviewEngine) getLastNonConfigurationInstance();

        if (result == null) {
            result = new BuildConfigurationOverviewEngine();
        }

        return result;
    }
}
