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

package com.tcity.android.ui.overview.project;

import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.tcity.android.app.DB;
import com.tcity.android.app.Preferences;
import com.tcity.android.db.CVUtils;
import com.tcity.android.db.DBUtils;
import com.tcity.android.db.Schema;
import com.tcity.android.loader.ExecutableRunnableChain;
import com.tcity.android.loader.LoaderPackage;
import com.tcity.android.loader.RunnableChain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectOverviewServerEngine {

    @NotNull
    private final String myProjectId;

    @NotNull
    private final Preferences myPreferences;

    @NotNull
    private final DB myDb;

    @NotNull
    private final ChainListener myChainListener;

    @Nullable
    private ExecutableRunnableChain myChain;

    ProjectOverviewServerEngine(@NotNull String projectId,
                                @NotNull Preferences preferences,
                                @NotNull DB db) {
        myProjectId = projectId;
        myPreferences = preferences;
        myDb = db;

        myChainListener = new ChainListener();
    }

    public void setActivity(@Nullable ProjectOverviewActivity activity) {
        myChainListener.myActivity = activity;

        if (myChainListener.count != 0 && activity != null) {
            activity.setRefreshing(true);
        }
    }

    public void projectImageClick(@NotNull String id) {
        if (isProjectFavourite(id)) {
            ExecutableRunnableChain statusTask = RunnableChain.getSingleRunnableChain(
                    LoaderPackage.getProjectStatusRunnable(id, myDb, myPreferences)
            ).toAsyncTask(myChainListener);

            myChainListener.onStarted();
            statusTask.execute();
        }
    }

    public void buildConfigurationImageClick(@NotNull String id) {
        if (isBuildConfigurationFavourite(id)) {
            ExecutableRunnableChain statusTask = RunnableChain.getSingleRunnableChain(
                    LoaderPackage.getBuildConfigurationStatusRunnable(id, myDb, myPreferences)
            ).toAsyncTask(myChainListener);

            myChainListener.onStarted();
            statusTask.execute();
        }
    }

    public void refresh() {
        if (myChain == null) {
            myChain = calculateExecutableChain();
        }

        if (myChain.getStatus() != AsyncTask.Status.RUNNING) {
            if (myChain.getStatus() == AsyncTask.Status.FINISHED) {
                myChain = calculateExecutableChain();
            }

            myChainListener.onStarted();
            myChain.execute();
        }
    }

    @NotNull
    private ExecutableRunnableChain calculateExecutableChain() {
        RunnableChain projectsChain = RunnableChain.getSingleRunnableChain(
                LoaderPackage.getProjectsRunnable(myDb, myPreferences)
        );

        RunnableChain buildConfigurationsChain = RunnableChain.getSingleRunnableChain(
                LoaderPackage.getBuildConfigurationsRunnable(myProjectId, myDb, myPreferences)
        );

        return RunnableChain.getOrRunnableChain(
                RunnableChain.getAndRunnableChain(
                        projectsChain,
                        calculateProjectStatusesChain()
                ),
                RunnableChain.getAndRunnableChain(
                        buildConfigurationsChain,
                        calculateBuildConfigurationStatusesChain()
                )
        ).toAsyncTask(myChainListener);
    }

    @NotNull
    private RunnableChain calculateProjectStatusesChain() {
        Cursor cursor = myDb.query(
                Schema.PROJECT,
                new String[]{Schema.TC_ID_COLUMN},
                Schema.PARENT_ID_COLUMN + " = ? AND " + Schema.FAVOURITE_COLUMN + " = ?",
                new String[]{myProjectId, CVUtils.toFavouriteContentValue(true)},
                null, null, null, null
        );

        Runnable[] runnables = new Runnable[cursor.getCount()];
        int pos = 0;

        while (cursor.moveToNext()) {
            runnables[pos] = LoaderPackage.getProjectStatusRunnable(
                    DBUtils.getId(cursor), myDb, myPreferences
            );

            pos++;
        }

        cursor.close();

        return RunnableChain.getOrRunnableChain(runnables);
    }

    @NotNull
    private RunnableChain calculateBuildConfigurationStatusesChain() {
        Cursor cursor = myDb.query(
                Schema.BUILD_CONFIGURATION,
                new String[]{Schema.TC_ID_COLUMN},
                Schema.PARENT_ID_COLUMN + " = ? AND " + Schema.FAVOURITE_COLUMN + " = ?",
                new String[]{myProjectId, CVUtils.toFavouriteContentValue(true)},
                null, null, null, null
        );

        Runnable[] runnables = new Runnable[cursor.getCount()];
        int pos = 0;

        while (cursor.moveToNext()) {
            runnables[pos] = LoaderPackage.getBuildConfigurationStatusRunnable(
                    DBUtils.getId(cursor), myDb, myPreferences
            );

            pos++;
        }

        cursor.close();

        return RunnableChain.getOrRunnableChain(runnables);
    }

    private boolean isProjectFavourite(@NotNull String id) {
        Cursor cursor = myDb.query(
                Schema.PROJECT,
                new String[]{Schema.FAVOURITE_COLUMN},
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{id},
                null, null, null, null
        );

        cursor.moveToNext();

        boolean result = DBUtils.getFavourite(cursor);

        cursor.close();

        return result;
    }

    private boolean isBuildConfigurationFavourite(@NotNull String id) {
        Cursor cursor = myDb.query(
                Schema.BUILD_CONFIGURATION,
                new String[]{Schema.FAVOURITE_COLUMN},
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{id},
                null, null, null, null
        );

        cursor.moveToNext();

        boolean result = DBUtils.getFavourite(cursor);

        cursor.close();

        return result;
    }

    private static class ChainListener implements RunnableChain.Listener {

        @Nullable
        private ProjectOverviewActivity myActivity;

        private int count;

        public void onStarted() {
            count++;

            if (myActivity != null) {
                myActivity.setRefreshing(true);
            }
        }

        @Override
        public void onFinished() {
            if (count != 0) {
                count--;
            }

            if (myActivity != null && count == 0) {
                myActivity.setRefreshing(false);
            }
        }

        @Override
        public void onException(@NotNull Exception e) {
            if (myActivity != null) {
                Toast.makeText(myActivity, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
