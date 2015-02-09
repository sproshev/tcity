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

package com.tcity.android.ui.current.overview;

import android.os.AsyncTask;

import com.tcity.android.background.HttpStatusException;
import com.tcity.android.background.parser.ParserPackage;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.db.DB;
import com.tcity.android.obj.QueuedBuild;
import com.tcity.android.obj.RunningBuild;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

class CurrentBuildsTask extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final RestClient myClient;

    @NotNull
    private final DB myDb;

    @Nullable
    private CurrentBuildsFragment myFragment;

    @Nullable
    private Exception myException;

    @Nullable
    private List<RunningBuild> myRunningBuilds;

    @Nullable
    private List<QueuedBuild> myQueuedBuilds;

    CurrentBuildsTask(@NotNull RestClient client, @NotNull DB db) {
        myClient = client;
        myDb = db;
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        try {
            loadRunningBuilds();
        } catch (IOException e) {
            myException = e;
        }

        try {
            loadQueuedBuilds();
        } catch (IOException e) {
            myException = e;
        }

        return null;
    }

    private void loadRunningBuilds() throws IOException {
        HttpResponse response = myClient.getRunningBuilds();

        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw new HttpStatusException(statusLine);
        } else {
            myRunningBuilds = ParserPackage.parseRunningBuilds(response.getEntity().getContent(), myDb);
        }
    }

    private void loadQueuedBuilds() throws IOException {
        HttpResponse response = myClient.getQueuedBuilds();

        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw new HttpStatusException(statusLine);
        } else {
            myQueuedBuilds = ParserPackage.parseQueuedBuilds(response.getEntity().getContent(), myDb);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (myFragment != null) {
            myFragment.onRefreshRunning();
        }
    }

    @Override
    protected void onPostExecute(@NotNull Void aVoid) {
        super.onPostExecute(aVoid);

        if (myFragment != null) {
            myFragment.onRefreshFinished();
        }
    }

    synchronized void setFragment(@Nullable CurrentBuildsFragment fragment) {
        myFragment = fragment;
    }

    @Nullable
    Exception getException() {
        return myException;
    }

    @Nullable
    List<RunningBuild> getRunningBuilds() {
        return myRunningBuilds;
    }

    @Nullable
    List<QueuedBuild> getQueuedBuilds() {
        return myQueuedBuilds;
    }
}
