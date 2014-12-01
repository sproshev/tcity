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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.tcity.android.app.Application;
import com.tcity.android.db.DbPackage;
import com.tcity.android.db.ProjectSchema;
import com.tcity.android.loader.LoaderPackage;
import com.tcity.android.loader.ProjectsRunnable;
import com.tcity.android.parser.ProjectsParser;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends ListActivity {

    @NotNull
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @NotNull
    private Application myApplication;

    @NotNull
    private OverviewListener myOverviewListener;

    @NotNull
    private OverviewEngine myOverviewEngine;

    @NotNull
    private Handler myProjectsHandler;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myApplication = (Application) getApplication();

        myOverviewListener = new OverviewListener() {
            @Override
            public void onProjectWatchClick(@NotNull String id) {
                if (myApplication.getPreferences().getWatchedProjectIds().contains(id)) {
                    myApplication.getPreferences().removeWatchedProjectId(id);
                    myApplication.getDBHelper().getWritableDatabase().update(ProjectSchema.INSTANCE$.getTableName(), DbPackage.contentValues(false, DbPackage.getWATCHED_COLUMN()), DbPackage.getTC_ID_COLUMN() + " = ?", new String[]{id});
                } else {
                    myApplication.getPreferences().addWatchedProjectId(id);
                    myApplication.getDBHelper().getWritableDatabase().update(ProjectSchema.INSTANCE$.getTableName(), DbPackage.contentValues(true, DbPackage.getWATCHED_COLUMN()), DbPackage.getTC_ID_COLUMN() + " = ?", new String[]{id});
                }

                myOverviewEngine.notifyProjectsChanged();
            }

            @Override
            public void onBuildConfigurationWatchClick(@NotNull String id) {

            }

            @Override
            public void onBuildWatchClick(@NotNull String id) {

            }

            @Override
            public void onProjectNameClick(@NotNull String id) {

            }

            @Override
            public void onBuildConfigurationNameClick(@NotNull String id) {

            }

            @Override
            public void onBuildNameClick(@NotNull String id) {

            }

            @Override
            public void onProjectOptionsClick(@NotNull String id, @NotNull View anchor) {

            }

            @Override
            public void onBuildConfigurationOptionsClick(@NotNull String id, @NotNull View anchor) {

            }

            @Override
            public void onBuildOptionsClick(@NotNull String id, @NotNull View anchor) {

            }
        };

        myOverviewEngine = new OverviewEngine(this, myApplication.getDBHelper(), myOverviewListener, "Projects", "Build Configurations", "Builds");

        myProjectsHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                if (msg.what == LoaderPackage.getERROR_CODE()) {
                    Log.w(LOG_TAG, (Exception) msg.obj);
                } else {
                    myOverviewEngine.notifyProjectsChanged();
                }
            }
        };

        getListView().setAdapter(myOverviewEngine.getAdapter());

        AsyncTask.execute(
                new ProjectsRunnable(
                        myApplication.getDBHelper(),
                        ProjectSchema.INSTANCE$,
                        ProjectsParser.INSTANCE$,
                        myApplication.getPreferences(),
                        myProjectsHandler
                )
        );
    }

    /* LIFECYCLE - END */
}
