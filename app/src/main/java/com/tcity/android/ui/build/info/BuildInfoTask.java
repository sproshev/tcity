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

package com.tcity.android.ui.build.info;

import android.os.AsyncTask;

import com.tcity.android.background.HttpStatusException;
import com.tcity.android.background.parser.ParserPackage;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.obj.BuildInfo;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildInfoTask extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final String myBuildId;

    @NotNull
    private final RestClient myClient;

    @Nullable
    private BuildInfoFragment myFragment;

    @Nullable
    private Exception myException;

    @Nullable
    private BuildInfo myResult;

    BuildInfoTask(@NotNull String buildId,
                  @NotNull RestClient client) {
        myBuildId = buildId;
        myClient = client;
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        try {
            HttpResponse response = myClient.getBuildInfo(myBuildId);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpStatusException(statusLine);
            } else {
                myResult = ParserPackage.parseBuildInfo(response.getEntity().getContent());
            }
        } catch (Exception e) {
            myException = e;
        }

        return null;
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

    synchronized void setFragment(@Nullable BuildInfoFragment fragment) {
        myFragment = fragment;
    }

    @Nullable
    Exception getException() {
        return myException;
    }

    @Nullable
    BuildInfo getResult() {
        return myResult;
    }
}
