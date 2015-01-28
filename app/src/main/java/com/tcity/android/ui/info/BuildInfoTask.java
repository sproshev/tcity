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

import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.HttpStatusException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

class BuildInfoTask extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final String myBuildId;

    @NotNull
    private final BuildInfoFragment myFragment;

    @NotNull
    private final RestClient myClient;

    @Nullable
    private Exception myException;

    @Nullable
    private Map<String, String> myResult;

    BuildInfoTask(@NotNull String buildId,
                  @NotNull BuildInfoFragment fragment,
                  @NotNull RestClient client) {
        myBuildId = buildId;
        myFragment = fragment;
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

        myFragment.onRefreshStarted();
    }

    @Override
    protected void onPostExecute(@NotNull Void aVoid) {
        super.onPostExecute(aVoid);

        myFragment.onRefreshFinished();
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

        String status = null;
        boolean isRunning = false;
        String branch = null;
        boolean isBranchDefault = false;
        String result = null;
        String waitReason = null;
        String queued = null;
        String started = null;
        String finished = null;
        String agent = null;

        try {
            reader.beginObject();

            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "status":
                        status = reader.nextString();
                        break;
                    case "running":
                        isRunning = reader.nextBoolean();
                        break;
                    case "branchName":
                        branch = reader.nextString();
                        break;
                    case "defaultBranch":
                        isBranchDefault = reader.nextBoolean();
                        break;
                    case "statusText":
                        result = reader.nextString();
                        break;
                    case "waitReason":
                        waitReason = reader.nextString();
                        break;
                    case "queuedDate":
                        queued = reader.nextString();
                        break;
                    case "startDate":
                        started = reader.nextString();
                        break;
                    case "finishDate":
                        finished = reader.nextString();
                        break;
                    case "agent":
                        agent = getAgentName(reader);
                        break;
                    default:
                        reader.skipValue();
                }
            }

            reader.endObject();

            calculateResult(
                    status,
                    isRunning,
                    branch,
                    isBranchDefault,
                    result,
                    waitReason,
                    queued,
                    started,
                    finished,
                    agent
            );
        } finally {
            reader.close();
        }
    }

    private void calculateResult(@Nullable String status,
                                 boolean isRunning,
                                 @Nullable String branch,
                                 boolean isBranchDefault,
                                 @Nullable String result,
                                 @Nullable String waitReason,
                                 @Nullable String queued,
                                 @Nullable String started,
                                 @Nullable String finished,
                                 @Nullable String agent) {
        myResult = new LinkedHashMap<>();

        if (isRunning) {
            myResult.put("Status", Status.RUNNING.toString());
        } else if (status != null) {
            myResult.put("Status", status);
        }

        if (branch != null) {
            myResult.put("Branch", branch + (isBranchDefault ? " (default)" : ""));
        }

        if (result != null) {
            myResult.put("Result", result);
        }

        if (waitReason != null) {
            myResult.put("Wait Reason", waitReason);
        }

        if (queued != null) {
            myResult.put("Queued", queued);
        }

        if (started != null) {
            myResult.put("Started", started);
        }

        if (finished != null) {
            myResult.put("Finished", finished);
        }

        if (agent != null) {
            myResult.put("Agent", agent);
        }
    }

    @Nullable
    private String getAgentName(@NotNull JsonReader reader) throws IOException {
        reader.beginObject();

        String result = null;

        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "name":
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
