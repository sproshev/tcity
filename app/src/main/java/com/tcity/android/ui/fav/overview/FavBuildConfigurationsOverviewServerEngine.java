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

package com.tcity.android.ui.fav.overview;

import android.database.Cursor;
import android.os.AsyncTask;

import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.BuildConfigurationStatusRunnable;
import com.tcity.android.background.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.background.runnable.chain.RunnableChain;
import com.tcity.android.db.DB;
import com.tcity.android.db.DBUtils;
import com.tcity.android.ui.ExpirationUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class FavBuildConfigurationsOverviewServerEngine {

    @NotNull
    private final DB myDB;

    @NotNull
    private final RestClient myClient;

    @NotNull
    private final ChainListener myChainListener;

    @Nullable
    private ExecutableRunnableChain myChain;

    FavBuildConfigurationsOverviewServerEngine(@NotNull DB db,
                                               @NotNull RestClient client) {
        myDB = db;
        myClient = client;

        myChainListener = new ChainListener();
    }

    void setFragment() {
        // TODO
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
        if (!force && !expiredBuildConfigurationStatusExists()) {
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

    private boolean expiredBuildConfigurationStatusExists() {
        Cursor cursor = myDB.getBuildConfigurations(null, true);

        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                String id = DBUtils.getId(cursor);

                if (ExpirationUtils.isBuildConfigurationStatusExpired(myDB, id)) {
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
        if (!force && !expiredBuildConfigurationStatusExists()) {
            return RunnableChain.getSingleRunnableChain(
                    new EmptyRunnable()
            ).toAsyncTask(myChainListener);
        }

        Cursor cursor = myDB.getBuildConfigurations(null, true);

        List<Runnable> runnables = new ArrayList<>();

        while (cursor.moveToNext()) {
            String id = DBUtils.getId(cursor);

            if (force || ExpirationUtils.isBuildConfigurationStatusExpired(myDB, id)) {
                runnables.add(
                        new BuildConfigurationStatusRunnable(
                                id, myDB, myClient
                        )
                );
            }
        }

        cursor.close();

        Runnable[] result = new Runnable[runnables.size()];

        return RunnableChain.getOrRunnableChain(
                runnables.toArray(result)
        ).toAsyncTask(myChainListener);
    }

    private static class ChainListener implements RunnableChain.Listener {

        private boolean myRunning;

        @Nullable
        private Exception myException;

        void onStarted() {
            myRunning = true;
            myException = null;

            // TODO
        }

        @Override
        public void onFinished() {
            myRunning = false;

            // TODO
        }

        @Override
        public void onException(@NotNull Exception e) {
            myException = e;

            // TODO
        }
    }

    private static class EmptyRunnable implements Runnable {

        @Override
        public void run() {
        }
    }
}
