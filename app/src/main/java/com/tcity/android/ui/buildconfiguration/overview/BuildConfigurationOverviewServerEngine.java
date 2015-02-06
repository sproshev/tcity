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

package com.tcity.android.ui.buildconfiguration.overview;

import android.os.AsyncTask;

import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.BuildsRunnable;
import com.tcity.android.background.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.background.runnable.chain.RunnableChain;
import com.tcity.android.db.DB;
import com.tcity.android.ui.ExpirationUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildConfigurationOverviewServerEngine {

    @NotNull
    private final String myBuildConfigurationId;

    @NotNull
    private final DB myDb;

    @NotNull
    private final ChainListener myChainListener;

    @NotNull
    private final RestClient myRestClient;

    @Nullable
    private ExecutableRunnableChain myChain;

    BuildConfigurationOverviewServerEngine(@NotNull String buildConfigurationId,
                                           @NotNull DB db,
                                           @NotNull RestClient restClient) {
        myBuildConfigurationId = buildConfigurationId;
        myDb = db;
        myRestClient = restClient;

        myChainListener = new ChainListener();
    }

    void setActivity(@Nullable BuildConfigurationOverviewActivity activity) {
        myChainListener.myActivity = activity;
    }

    boolean isRefreshing() {
        return myChainListener.myRunning;
    }

    @Nullable
    Exception getException() {
        return myChainListener.myException;
    }

    void resetException() {
        myChainListener.myException = null;
    }

    void refresh(boolean force) {
        if (!force && !ExpirationUtils.areBuildsExpired(myDb, myBuildConfigurationId)) {
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

    @NotNull
    private ExecutableRunnableChain calculateExecutableChain() {
        return RunnableChain.getSingleRunnableChain(
                new BuildsRunnable(myBuildConfigurationId, myDb, myRestClient)
        ).toAsyncTask(myChainListener);
    }

    private static class ChainListener implements RunnableChain.Listener {

        @Nullable
        private BuildConfigurationOverviewActivity myActivity;

        private boolean myRunning;

        @Nullable
        private Exception myException;

        private void onStarted() {
            myRunning = true;
            myException = null;

            if (myActivity != null) {
                myActivity.onRefreshRunning();
            }
        }

        @Override
        public void onFinished() {
            myRunning = false;

            if (myActivity != null) {
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
