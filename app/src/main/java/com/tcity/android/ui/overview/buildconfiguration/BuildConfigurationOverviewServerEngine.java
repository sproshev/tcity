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

import android.os.AsyncTask;
import android.widget.Toast;

import com.tcity.android.app.DB;
import com.tcity.android.app.Preferences;
import com.tcity.android.client.runnable.RunnablePackage;
import com.tcity.android.client.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.client.runnable.chain.RunnableChain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildConfigurationOverviewServerEngine {

    @NotNull
    private final String myBuildConfigurationId;

    @NotNull
    private final Preferences myPreferences;

    @NotNull
    private final DB myDb;

    @NotNull
    private final ChainListener myChainListener;

    @Nullable
    private ExecutableRunnableChain myChain;

    BuildConfigurationOverviewServerEngine(@NotNull String buildConfigurationId,
                                           @NotNull Preferences preferences,
                                           @NotNull DB db) {
        myBuildConfigurationId = buildConfigurationId;
        myPreferences = preferences;
        myDb = db;

        myChainListener = new ChainListener();
    }

    public void setActivity(@Nullable BuildConfigurationOverviewActivity activity) {
        myChainListener.myActivity = activity;

        if (myChainListener.count != 0 && activity != null) {
            activity.setRefreshing(true);
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
        return RunnableChain.getSingleRunnableChain(
                RunnablePackage.getBuildsRunnable(myBuildConfigurationId, myDb, myPreferences)
        ).toAsyncTask(myChainListener);
    }

    private static class ChainListener implements RunnableChain.Listener {

        @Nullable
        private BuildConfigurationOverviewActivity myActivity;

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
