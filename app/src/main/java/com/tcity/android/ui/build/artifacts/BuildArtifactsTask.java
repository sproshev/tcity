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
import android.util.JsonReader;

import com.tcity.android.background.HttpStatusException;
import com.tcity.android.background.rest.RestClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
                handleResponse(response);
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
            myFragment.onRefreshStarted();
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

    private void handleResponse(@NotNull HttpResponse response) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(response.getEntity().getContent()));

        //noinspection TryFinallyCanBeTryWithResources
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "file":
                        handleFiles(reader);
                        break;
                    default:
                        reader.skipValue();
                }
            }

            reader.endObject();
        } finally {
            reader.close();
        }
    }

    private void handleFiles(@NotNull JsonReader reader) throws IOException {
        reader.beginArray();

        List<BuildArtifact> result = new ArrayList<>();

        while (reader.hasNext()) {
            reader.beginObject();

            long size = -1;
            String name = null;
            String contentHref = null;
            String childrenHref = null;

            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "size":
                        size = reader.nextLong();
                        break;
                    case "name":
                        name = reader.nextString();
                        break;
                    case "children":
                        childrenHref = getHref(reader);
                        break;
                    case "content":
                        contentHref = getHref(reader);
                        break;
                    default:
                        reader.skipValue();
                }
            }

            if (name == null) {
                throw new IllegalStateException("Invalid artifacts json: \"name\" is absent");
            }

            if (contentHref == null && childrenHref == null) {
                throw new IllegalStateException(
                        "Invalid artifacts json: \"content\" and \"children\" are absent"
                );
            }

            result.add(new BuildArtifact(size, name, contentHref, childrenHref));

            reader.endObject();
        }

        reader.endArray();

        myResult = result;
    }

    @Nullable
    private String getHref(@NotNull JsonReader reader) throws IOException {
        reader.beginObject();

        String result = null;

        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "href":
                    result = reader.nextString();
                    break;
                default:
                    reader.skipValue();
            }
        }

        reader.endObject();

        return result;
    }
}
