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

package com.tcity.android.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.db.DbPackage;
import com.tcity.android.db.ProjectSchema;
import com.tcity.android.loader.LoaderPackage;
import com.tcity.android.loader.ProjectsRunnable;
import com.tcity.android.parser.ProjectsParser;
import com.tcity.android.rest.RestPackage;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private Application myApplication;

    @NotNull
    private OverviewEngine myOverviewEngine;

    @NotNull
    private Handler myProjectsHandler;

    @NotNull
    private ProjectsRunnable myProjectsRunnable;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.overview);

        myLayout = (SwipeRefreshLayout) findViewById(R.id.overview_layout);
        myLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_purple); // TODO change colors
        myLayout.setOnRefreshListener(this);

        myApplication = (Application) getApplication();

        myOverviewEngine = new OverviewEngine(this, myApplication.getDB(), "Projects", "Build Configurations", "Builds");
        myOverviewEngine.setListener(new OverviewListener());

        myProjectsHandler = new ProjectsHandler();

        getListView().setAdapter(myOverviewEngine.getAdapter());

        myProjectsRunnable = new ProjectsRunnable(
                myApplication.getDB(),
                ProjectSchema.INSTANCE$,
                ProjectsParser.INSTANCE$,
                myApplication.getPreferences(),
                myProjectsHandler
        );

        loadAllData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        myProjectsHandler.disable();
    }

    /* LIFECYCLE - END */

    @Override
    public void onRefresh() {
        loadAllData();
    }

    private void loadAllData() {
        myLayout.setRefreshing(true);

        AsyncTask.execute(myProjectsRunnable);
    }

    private class OverviewListener implements com.tcity.android.ui.OverviewListener {
        @Override
        public void onProjectWatchClick(@NotNull String id) {
            boolean watched = myApplication.getPreferences().getWatchedProjectIds().contains(id);

            if (watched) {
                myApplication.getPreferences().removeWatchedProjectId(id);
            } else {
                myApplication.getPreferences().addWatchedProjectId(id);
            }

            myApplication.getDB().update(
                    ProjectSchema.INSTANCE$,
                    DbPackage.contentValues(!watched, DbPackage.getWATCHED_COLUMN()),
                    DbPackage.getTC_ID_COLUMN() + " = ?",
                    new String[]{id}
            );

            myOverviewEngine.notifyProjectsChanged();
        }

        @Override
        public void onBuildConfigurationWatchClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onBuildWatchClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onProjectNameClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onBuildConfigurationNameClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onBuildNameClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onProjectOptionsClick(@NotNull String id, @NotNull View anchor) {
            PopupMenu menu = new PopupMenu(MainActivity.this, anchor);

            menu.inflate(R.menu.menu_concept);

            menu.setOnMenuItemClickListener(new ProjectMenuItemClickListener(id));

            menu.show();
        }

        @Override
        public void onBuildConfigurationOptionsClick(@NotNull String id, @NotNull View anchor) {
            // TODO
        }

        @Override
        public void onBuildOptionsClick(@NotNull String id, @NotNull View anchor) {
            // TODO
        }
    }

    private class ProjectMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        @NotNull
        private final String myId;

        private ProjectMenuItemClickListener(@NotNull String id) {
            myId = id;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_share:
                    onShareClick();

                    return true;
                case R.id.menu_details:
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
                    RestPackage.getProjectWebUrl(myId, myApplication.getPreferences())
            );

            startActivity(Intent.createChooser(intent, "Share"));
        }
    }

    private class Handler extends android.os.Handler {

        protected boolean disabled = false;

        public void disable() {
            disabled = true;
        }
    }

    private class ProjectsHandler extends Handler {

        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);

            if (disabled) {
                return;
            }

            myLayout.setRefreshing(false);

            if (msg.what == LoaderPackage.getERROR_CODE()) {
                Toast.makeText(
                        MainActivity.this,
                        ((Exception) msg.obj).getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                myOverviewEngine.notifyProjectsChanged();
            }
        }
    }
}
