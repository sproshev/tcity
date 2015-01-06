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

import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.background.runnable.chain.RunnableChain;
import com.tcity.android.background.runnable.primitive.BuildConfigurationStatusRunnable;
import com.tcity.android.background.runnable.primitive.BuildConfigurationsRunnable;
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
    private final ChainListener myChainListener;
    @NotNull
    private final RestClient myRestClient;
    @Nullable
    private ExecutableRunnableChain myChain;

    ProjectOverviewServerEngine(@NotNull String projectId,
                                @NotNull DB db,
                                @NotNull RestClient restClient) {
        myProjectId = projectId;
        myDb = db;

        myChainListener = new ChainListener();
        myRestClient = restClient;
    }

    public void setActivity(@Nullable ProjectOverviewActivity activity) {
        myChainListener.myActivity = activity;

        if (myChainListener.myCount != 0 && activity != null) {
            activity.setRefreshing(true);
        }
    }

    public void projectImageClick(@NotNull String id) {
        if (myDb.isProjectFavourite(id)) {
            ExecutableRunnableChain statusTask = RunnableChain.getSingleRunnableChain(
                    new ProjectStatusRunnable(id, myDb, myRestClient)
            ).toAsyncTask(myChainListener);

            myChainListener.onStarted();
            statusTask.execute();
        }
    }

    public void buildConfigurationImageClick(@NotNull String id) {
        if (myDb.isBuildConfigurationFavourite(id)) {
            ExecutableRunnableChain statusTask = RunnableChain.getSingleRunnableChain(
                    new BuildConfigurationStatusRunnable(id, myDb, myRestClient)
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
                new ProjectsRunnable(myDb, myRestClient)
        );

        RunnableChain buildConfigurationsChain = RunnableChain.getSingleRunnableChain(
                new BuildConfigurationsRunnable(myProjectId, myDb, myRestClient)
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

        public void onStarted() {
            myCount++;

            if (myActivity != null) {
                myActivity.setRefreshing(true);
            }
        }

        @Override
        public void onFinished() {
            if (myCount != 0) {
                myCount--;
            }

            if (myActivity != null && myCount == 0) {
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
