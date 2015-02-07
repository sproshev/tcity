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

package com.tcity.android.ui.build.artifacts;

import android.os.AsyncTask;

import com.tcity.android.background.HttpStatusException;
import com.tcity.android.background.parser.ParserPackage;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.obj.BuildArtifact;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

class BuildArtifactsTask extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final String myBuildId;

    @Nullable
    private final String myPath;

    @NotNull
    private final RestClient myClient;

    @Nullable
    private BuildArtifactsFragment myFragment;

    @Nullable
    private Exception myException;

    @Nullable
    private List<BuildArtifact> myResult;

    BuildArtifactsTask(@NotNull String buildId,
                       @Nullable String path,
                       @NotNull RestClient client) {
        myBuildId = buildId;
        myPath = path;
        myClient = client;
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        try {
            HttpResponse response = getHttpResponse();
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpStatusException(statusLine);
            } else {
                myResult = ParserPackage.parseBuildArtifacts(response.getEntity().getContent());
            }
        } catch (Exception e) {
            myException = null;
        }

        return null;
    }

    @NotNull
    private HttpResponse getHttpResponse() throws IOException {
        return myPath == null
                ? myClient.getBuildArtifacts(myBuildId)
                : myClient.getBuildArtifactChildren(myPath);
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

    synchronized void setFragment(@Nullable BuildArtifactsFragment fragment) {
        myFragment = fragment;
    }

    @Nullable
    Exception getException() {
        return myException;
    }

    @Nullable
    List<BuildArtifact> getResult() {
        return myResult;
    }
}
