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

import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.BuildConfigurationStatusRunnable;
import com.tcity.android.background.runnable.BuildConfigurationsRunnable;
import com.tcity.android.background.runnable.FavouriteProjectsRunnable;
import com.tcity.android.background.runnable.ProjectStatusRunnable;
import com.tcity.android.background.runnable.ProjectsRunnable;
import com.tcity.android.background.runnable.ServerVersionRunnable;
import com.tcity.android.background.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.background.runnable.chain.RunnableChain;
import com.tcity.android.db.DB;
import com.tcity.android.db.DBUtils;
import com.tcity.android.ui.ExpirationUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class ProjectOverviewServerEngine {

    @NotNull
    private final String myProjectId;

    @NotNull
    private final DB myDb;

    @NotNull
    private final RestClient myClient;

    @NotNull
    private final Preferences myPreferences;

    @NotNull
    private final ChainListener myChainListener;

    @Nullable
    private ExecutableRunnableChain myChain;

    ProjectOverviewServerEngine(@NotNull String projectId,
                                @NotNull DB db,
                                @NotNull RestClient client,
                                @NotNull Preferences preferences) {
        myProjectId = projectId;
        myDb = db;
        myClient = client;
        myPreferences = preferences;

        myChainListener = new ChainListener();
    }

    void setListener(@Nullable ProjectOverviewListener listener) {
        myChainListener.myListener = listener;
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
                    new ProjectStatusRunnable(id, myDb, myClient)
            ).toAsyncTask(myChainListener);

            myChainListener.onStarted();
            statusTask.execute();
        }
    }

    void buildConfigurationImageClick(@NotNull String id) {
        if (myDb.isBuildConfigurationFavourite(id)) {
            ExecutableRunnableChain statusTask = RunnableChain.getSingleRunnableChain(
                    new BuildConfigurationStatusRunnable(id, myDb, myClient)
            ).toAsyncTask(myChainListener);

            myChainListener.onStarted();
            statusTask.execute();
        }
    }

    void refresh(boolean force) {
        if (!force &&
                !ExpirationUtils.areProjectsExpired(myDb) &&
                !ExpirationUtils.areBuildConfigurationsExpired(myDb, myProjectId) &&
                !expiredProjectStatusExists() &&
                !expiredBuildConfigurationStatusExists() &&
                !ExpirationUtils.areFavouriteProjectsExpired(myPreferences) &&
                !ExpirationUtils.isServerVersionExpired(myPreferences)) {
            return;
        }

        if (myChain == null) {
            myChain = calculateExecutableChain(force);
        }

        if (myChain.getStatus() != AsyncTask.Status.RUNNING) {
            if (myChain.getStatus() == AsyncTask.Status.FINISHED) {
                myChain = calculateExecutableChain(force);
            }

            myChainListener.onStarted();
            myChain.execute();
        }
    }

    private boolean expiredProjectStatusExists() {
        Cursor cursor = myDb.getProjects(myProjectId, true);

        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                String id = DBUtils.getId(cursor);

                if (ExpirationUtils.isProjectStatusExpired(myDb, id)) {
                    return true;
                }
            }
        } finally {
            cursor.close();
        }

        return false;
    }

    private boolean expiredBuildConfigurationStatusExists() {
        Cursor cursor = myDb.getBuildConfigurations(myProjectId, true);

        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                String id = DBUtils.getId(cursor);

                if (ExpirationUtils.isBuildConfigurationStatusExpired(myDb, id)) {
                    return true;
                }
            }
        } finally {
            cursor.close();
        }

        return false;
    }

    @NotNull
    private ExecutableRunnableChain calculateExecutableChain(boolean force) {
        RunnableChain fullProjectsChain = RunnableChain.getAndRunnableChain(
                getProjectsChain(force),
                calculateProjectStatusesChain(force)
        );

        RunnableChain fullBuildConfigurationChain = RunnableChain.getAndRunnableChain(
                getBuildConfigurationsChain(force),
                calculateBuildConfigurationStatusesChain(force)
        );

        return RunnableChain.getOrRunnableChain(
                getServerVersionChain(force),
                getFavouriteProjectsChain(force),
                fullProjectsChain,
                fullBuildConfigurationChain
        ).toAsyncTask(myChainListener);
    }

    @NotNull
    private RunnableChain getProjectsChain(boolean force) {
        if (!force && !ExpirationUtils.areProjectsExpired(myDb)) {
            return RunnableChain.getSingleRunnableChain(new EmptyRunnable());
        }

        return RunnableChain.getSingleRunnableChain(
                new ProjectsRunnable(myDb, myClient)
        );
    }

    @NotNull
    private RunnableChain getBuildConfigurationsChain(boolean force) {
        if (!force && !ExpirationUtils.areBuildConfigurationsExpired(myDb, myProjectId)) {
            return RunnableChain.getSingleRunnableChain(new EmptyRunnable());
        }

        return RunnableChain.getSingleRunnableChain(
                new BuildConfigurationsRunnable(myProjectId, myDb, myClient)
        );
    }

    @NotNull
    private RunnableChain getServerVersionChain(boolean force) {
        if (!force && !ExpirationUtils.isServerVersionExpired(myPreferences)) {
            return RunnableChain.getSingleRunnableChain(new EmptyRunnable());
        }

        return RunnableChain.getSingleRunnableChain(
                new ServerVersionRunnable(myClient, myPreferences)
        );
    }

    @NotNull
    private RunnableChain getFavouriteProjectsChain(boolean force) {
        if (!force && !ExpirationUtils.areFavouriteProjectsExpired(myPreferences)) {
            return RunnableChain.getSingleRunnableChain(new EmptyRunnable());
        }

        return RunnableChain.getSingleRunnableChain(
                new FavouriteProjectsRunnable(
                        myDb, myClient, myPreferences
                )
        );
    }

    @NotNull
    private RunnableChain calculateProjectStatusesChain(boolean force) {
        if (!force && !expiredProjectStatusExists()) {
            return RunnableChain.getSingleRunnableChain(new EmptyRunnable());
        }

        Cursor cursor = myDb.getProjects(myProjectId, true);

        List<Runnable> runnables = new ArrayList<>();

        while (cursor.moveToNext()) {
            String id = DBUtils.getId(cursor);

            if (force || ExpirationUtils.isProjectStatusExpired(myDb, id)) {
                runnables.add(
                        new ProjectStatusRunnable(
                                id, myDb, myClient
                        )
                );
            }
        }

        cursor.close();

        Runnable[] result = new Runnable[runnables.size()];

        return RunnableChain.getOrRunnableChain(runnables.toArray(result));
    }

    @NotNull
    private RunnableChain calculateBuildConfigurationStatusesChain(boolean force) {
        if (!force && !expiredBuildConfigurationStatusExists()) {
            return RunnableChain.getSingleRunnableChain(new EmptyRunnable());
        }

        Cursor cursor = myDb.getBuildConfigurations(myProjectId, true);

        List<Runnable> runnables = new ArrayList<>();

        while (cursor.moveToNext()) {
            String id = DBUtils.getId(cursor);

            if (force || ExpirationUtils.isBuildConfigurationStatusExpired(myDb, id)) {
                runnables.add(
                        new BuildConfigurationStatusRunnable(
                                id, myDb, myClient
                        )
                );
            }
        }

        cursor.close();

        Runnable[] result = new Runnable[runnables.size()];

        return RunnableChain.getOrRunnableChain(runnables.toArray(result));
    }

    private static class ChainListener implements RunnableChain.Listener {

        @Nullable
        private ProjectOverviewListener myListener;

        private int myCount;

        @Nullable
        private Exception myException;

        private void onStarted() {
            if (myCount == 0) {
                myException = null;
            }

            if (myListener != null && myCount == 0) {
                myListener.onRefreshRunning();
            }

            myCount++;
        }

        @Override
        public void onFinished() {
            if (myCount != 0) {
                myCount--;
            }

            if (myListener != null && myCount == 0) {
                myListener.onRefreshFinished();
            }
        }

        @Override
        public void onException(@NotNull Exception e) {
            myException = e;

            if (myListener != null) {
                myListener.onRefreshException();
            }
        }
    }

    private static class EmptyRunnable implements Runnable {

        @Override
        public void run() {
        }
    }
}
