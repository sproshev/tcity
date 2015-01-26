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

import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.background.runnable.chain.RunnableChain;
import com.tcity.android.background.runnable.primitive.BuildConfigurationStatusRunnable;
import com.tcity.android.background.runnable.primitive.BuildConfigurationsRunnable;
import com.tcity.android.background.runnable.primitive.FavouriteProjectsRunnable;
import com.tcity.android.background.runnable.primitive.ProjectStatusRunnable;
import com.tcity.android.background.runnable.primitive.ProjectsRunnable;
import com.tcity.android.db.DB;
import com.tcity.android.db.DBUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectOverviewServerEngine {

    @NotNull
    private final String myProjectId;

    @NotNull
    private final DB myDb;

    @NotNull
    private final RestClient myRestClient;

    @NotNull
    private final Preferences myPreferences;

    @NotNull
    private final ChainListener myChainListener;

    @Nullable
    private ExecutableRunnableChain myChain;

    ProjectOverviewServerEngine(@NotNull String projectId,
                                @NotNull DB db,
                                @NotNull RestClient restClient,
                                @NotNull Preferences preferences) {
        myProjectId = projectId;
        myDb = db;
        myRestClient = restClient;
        myPreferences = preferences;

        myChainListener = new ChainListener();
    }

    void setActivity(@Nullable ProjectOverviewActivity activity) {
        myChainListener.myActivity = activity;
    }

    boolean isRefreshing() {
        return myChainListener.myCount != 0;
    }

    @Nullable
    Exception getException() {
        return myChainListener.myException;
    }

    void resetException() {
        myChainListener.myException = null;
    }

    void projectImageClick(@NotNull String id) {
        if (myDb.isProjectFavourite(id)) {
            ExecutableRunnableChain statusTask = RunnableChain.getSingleRunnableChain(
                    new ProjectStatusRunnable(id, myDb, myRestClient)
            ).toAsyncTask(myChainListener);

            myChainListener.onStarted();
            statusTask.execute();
        }
    }

    void buildConfigurationImageClick(@NotNull String id) {
        if (myDb.isBuildConfigurationFavourite(id)) {
            ExecutableRunnableChain statusTask = RunnableChain.getSingleRunnableChain(
                    new BuildConfigurationStatusRunnable(id, myDb, myRestClient)
            ).toAsyncTask(myChainListener);

            myChainListener.onStarted();
            statusTask.execute();
        }
    }

    void refresh(boolean force) {
        if (!force &&
                !areProjectsExpired() &&
                !areBuildConfigurationsExpired() &&
                !expiredProjectStatusExists() &&
                !expiredBuildConfigurationStatusExists() &&
                !areFavouriteProjectsExpired()) {
            return;
        }

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

    private boolean areProjectsExpired() {
        return true;
    }

    private boolean areBuildConfigurationsExpired() {
        return true;
    }

    private boolean expiredProjectStatusExists() {
        return true;
    }

    private boolean expiredBuildConfigurationStatusExists() {
        return true;
    }

    private boolean areFavouriteProjectsExpired() {
        return true;
    }

    @NotNull
    private ExecutableRunnableChain calculateExecutableChain() {
        // TODO customize

        RunnableChain projectsChain = RunnableChain.getSingleRunnableChain(
                new ProjectsRunnable(myDb, myRestClient)
        );

        RunnableChain buildConfigurationsChain = RunnableChain.getSingleRunnableChain(
                new BuildConfigurationsRunnable(myProjectId, myDb, myRestClient)
        );

        RunnableChain fullProjectsChain = RunnableChain.getAndRunnableChain(
                projectsChain,
                calculateProjectStatusesChain()
        );

        RunnableChain fullBuildConfigurationChain = RunnableChain.getAndRunnableChain(
                buildConfigurationsChain,
                calculateBuildConfigurationStatusesChain()
        );

        return RunnableChain.getOrRunnableChain(
                RunnableChain.getSingleRunnableChain(
                        new FavouriteProjectsRunnable(myPreferences.getLogin(), myDb, myRestClient)
                ),
                fullProjectsChain,
                fullBuildConfigurationChain
        ).toAsyncTask(myChainListener);
    }

    @NotNull
    private RunnableChain calculateProjectStatusesChain() {
        Cursor cursor = myDb.getProjects(myProjectId, true);

        Runnable[] runnables = new Runnable[cursor.getCount()];
        int pos = 0;

        while (cursor.moveToNext()) {
            runnables[pos] = new ProjectStatusRunnable(
                    DBUtils.getId(cursor), myDb, myRestClient
            );

            pos++;
        }

        cursor.close();

        return RunnableChain.getOrRunnableChain(runnables);
    }

    @NotNull
    private RunnableChain calculateBuildConfigurationStatusesChain() {
        Cursor cursor = myDb.getBuildConfigurations(myProjectId, true);

        Runnable[] runnables = new Runnable[cursor.getCount()];
        int pos = 0;

        while (cursor.moveToNext()) {
            runnables[pos] = new BuildConfigurationStatusRunnable(
                    DBUtils.getId(cursor), myDb, myRestClient
            );

            pos++;
        }

        cursor.close();

        return RunnableChain.getOrRunnableChain(runnables);
    }

    private static class ChainListener implements RunnableChain.Listener {

        @Nullable
        private ProjectOverviewActivity myActivity;

        private int myCount;

        @Nullable
        private Exception myException;

        void onStarted() {
            if (myCount == 0) {
                myException = null;
            }

            if (myActivity != null && myCount == 0) {
                myActivity.onRefreshRunning();
            }

            myCount++;
        }

        @Override
        public void onFinished() {
            if (myCount != 0) {
                myCount--;
            }

            if (myActivity != null && myCount == 0) {
                myActivity.onRefreshFinished();
            }
        }

        @Override
        public void onException(@NotNull Exception e) {
            myException = e;

            if (myActivity != null) {
                myActivity.onRefreshException();
            }
        }
    }
}
