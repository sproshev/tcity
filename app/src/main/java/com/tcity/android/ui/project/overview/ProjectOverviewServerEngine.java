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

import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.tcity.android.app.DB;
import com.tcity.android.app.Preferences;
import com.tcity.android.db.DbPackage;
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

    public void setActivity(@Nullable ProjectOverviewActivity activity) {
        myChainListener.myActivity = activity;

        if (myChainListener.isRunning && activity != null) {
            activity.setRefreshing(true);
        }
    }

    @NotNull
    private ExecutableRunnableChain calculateExecutableChain() {
        RunnableChain projectsChain = RunnableChain.getSingleRunnableChain(
                LoaderPackage.getProjectsRunnable(myDb, myPreferences)
        );

        return RunnableChain.getAndRunnableChain(
                projectsChain,
                calculateStatusesChain()
        ).toAsyncTask(myChainListener);
    }

    @NotNull
    private RunnableChain calculateStatusesChain() {
        Cursor cursor = myDb.query(
                Schema.PROJECT,
                new String[]{Schema.TC_ID_COLUMN},
                Schema.PARENT_ID_COLUMN + " = ? AND " + Schema.WATCHED_COLUMN + " = ?",
                new String[]{myProjectId, Integer.toString(DbPackage.getDbValue(true))},
                null, null, null, null
        );

        Runnable[] runnables = new Runnable[cursor.getCount()];
        int pos = 0;

        while (cursor.moveToNext()) {
            runnables[pos] = LoaderPackage.getProjectStatusRunnable(
                    DbPackage.getId(cursor), myDb, myPreferences
            );

            pos++;
        }

        cursor.close();

        return RunnableChain.getOrRunnableChain(runnables);
    }

    private static class ChainListener implements RunnableChain.Listener {

        @Nullable
        private ProjectOverviewActivity myActivity;

        private boolean isRunning;

        public void onStarted() {
            if (myActivity != null) {
                myActivity.setRefreshing(true);
                isRunning = true;
            }
        }

        @Override
        public void onFinished() {
            if (myActivity != null) {
                myActivity.setRefreshing(false);
                isRunning = false;
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
