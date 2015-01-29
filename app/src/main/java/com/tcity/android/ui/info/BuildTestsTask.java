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

package com.tcity.android.ui.info;

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
import java.util.LinkedHashMap;
import java.util.Map;

class BuildTestsTask extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final String myBuildId;

    @NotNull
    private final RestClient myClient;

    @Nullable
    private BuildTestsFragment myFragment;

    @Nullable
    private Exception myException;

    @Nullable
    private Map<String, String> myResult;

    BuildTestsTask(@NotNull String buildId,
                   @NotNull RestClient client) {
        myBuildId = buildId;
        myClient = client;
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        try {
            HttpResponse response = myClient.getBuildTests(myBuildId);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpStatusException(statusLine);
            } else {
                handleResponse(response);
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

    synchronized void setFragment(@Nullable BuildTestsFragment fragment) {
        myFragment = fragment;
    }

    @Nullable
    Exception getException() {
        return myException;
    }

    @Nullable
    Map<String, String> getResult() {
        return myResult;
    }

    private void handleResponse(@NotNull HttpResponse response) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(response.getEntity().getContent()));

        //noinspection TryFinallyCanBeTryWithResources
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "testOccurrence":
                        handleTests(reader);
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

    private void handleTests(@NotNull JsonReader reader) throws IOException {
        reader.beginArray();

        Map<String, String> result = new LinkedHashMap<>();

        while (reader.hasNext()) {
            reader.beginObject();

            String name = null;
            String status = null;

            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "name":
                        name = reader.nextString();
                        break;
                    case "status":
                        status = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                }
            }

            if (name != null && status != null) {
                result.put(name, status);
            } else {
                if (name == null) {
                    throw new IOException("Invalid tests json: \"name\" is absent");
                } else {
                    throw new IOException("Invalid tests json: \"status\" is absent");
                }
            }

            reader.endObject();
        }

        reader.endArray();

        myResult = result;
    }
}
