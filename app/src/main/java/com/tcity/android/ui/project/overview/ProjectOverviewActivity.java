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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.DB;
import com.tcity.android.app.Preferences;
import com.tcity.android.concept.ConceptPackage;
import com.tcity.android.db.DbPackage;
import com.tcity.android.db.Schema;
import com.tcity.android.rest.RestPackage;
import com.tcity.android.ui.BuildConfigurationOverviewActivity;

import org.jetbrains.annotations.NotNull;

public class ProjectOverviewActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    public static final String INTENT_KEY = "PROJECT_ID";

    @NotNull
    private String myProjectId;

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private ProjectOverviewEngine myEngine;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myProjectId = calculateProjectId();

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

    void projectImageClick(@NotNull String id) {
        myEngine.projectImageClick(id);
    }

    void projectNameClick(@NotNull String id) {
        Intent intent = new Intent(this, ProjectOverviewActivity.class);
        intent.putExtra(INTENT_KEY, id);

        startActivity(intent);
    }

    void projectOptionsClick(@NotNull String id, @NotNull View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.inflate(R.menu.menu_concept);

        menu.setOnMenuItemClickListener(
                new PopupMenuListener(
                        RestPackage.getProjectWebUrl(id, new Preferences(this))
                )
        );

        menu.show();
    }

    void buildConfigurationImageClick(@NotNull String id) {
        myEngine.buildConfigurationImageClick(id);
    }

    void buildConfigurationNameClick(@NotNull String id) {
        Intent intent = new Intent(this, BuildConfigurationOverviewActivity.class);
        intent.putExtra("BUILD_CONFIGURATION_ID", id); // TODO key

        startActivity(intent);
    }

    void buildConfigurationOptionsClick(@NotNull String id, @NotNull View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.inflate(R.menu.menu_concept);

        menu.setOnMenuItemClickListener(
                new PopupMenuListener(
                        RestPackage.getBuildConfigurationWebUrl(id, new Preferences(this))
                )
        );

        menu.show();
    }

    @NotNull
    private String calculateProjectId() {
        if (getIntent().hasExtra(INTENT_KEY)) {
            return getIntent().getStringExtra(INTENT_KEY);
        } else {
            return ConceptPackage.getROOT_PROJECT_ID();
        }
    }

    @NotNull
    private String calculateTitle() {
        if (myProjectId.equals(ConceptPackage.getROOT_PROJECT_ID())) {
            return getString(R.string.projects);
        }

        DB db = ((Application) getApplication()).getDB();

        Cursor cursor = db.query(
                Schema.PROJECT,
                new String[]{Schema.NAME_COLUMN},
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{myProjectId},
                null, null, null, null
        );

        cursor.moveToNext();

        String result = DbPackage.getName(cursor);

        cursor.close();

        return result;
    }

    @NotNull
    private ProjectOverviewEngine calculateEngine() {
        @SuppressWarnings("deprecation")
        ProjectOverviewEngine result = (ProjectOverviewEngine) getLastNonConfigurationInstance();

        if (result == null) {
            result = new ProjectOverviewEngine(
                    myProjectId,
                    this,
                    ((Application) getApplication()).getDB(),
                    getListView()
            );
        }

        return result;
    }

    private class PopupMenuListener implements PopupMenu.OnMenuItemClickListener {

        @NotNull
        private final String myUrl;

        private PopupMenuListener(@NotNull String url) {
            myUrl = url;
        }

        @Override
        public boolean onMenuItemClick(@NotNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_share:
                    onShareClick();

                    return true;
                default:
                    return false;
            }
        }

        private void onShareClick() {
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    myUrl
            );

            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share)));
        }
    }
}
