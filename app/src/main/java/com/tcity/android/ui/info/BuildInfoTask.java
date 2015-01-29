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
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
    private BuildInfoData myResult;

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
    BuildInfoData getResult() {
        return myResult;
    }

    private void handleResponse(@NotNull HttpResponse response) throws IOException, ParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(response.getEntity().getContent()));

        //noinspection TryFinallyCanBeTryWithResources
        try {
            reader.beginObject();

            BuildInfoData data = new BuildInfoData();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");

            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "status":
                        if (data.status == null) {
                            data.status = com.tcity.android.Status.valueOf(reader.nextString());
                        }
                        break;
                    case "running":
                        if (reader.nextBoolean()) {
                            data.status = com.tcity.android.Status.RUNNING;
                        }
                        break;
                    case "branchName":
                        data.branch = reader.nextString();
                        break;
                    case "defaultBranch":
                        data.isBranchDefault = reader.nextBoolean();
                        break;
                    case "statusText":
                        data.result = reader.nextString();
                        break;
                    case "waitReason":
                        data.waitReason = reader.nextString();
                        break;
                    case "queuedDate":
                        data.queued = dateFormat.parse(reader.nextString());
                        break;
                    case "startDate":
                        data.started = dateFormat.parse(reader.nextString());
                        break;
                    case "finishDate":
                        data.finished = dateFormat.parse(reader.nextString());
                        break;
                    case "agent":
                        data.agent = getAgentName(reader);
                        break;
                    default:
                        reader.skipValue();
                }
            }

            myResult = data;

            reader.endObject();
        } finally {
            reader.close();
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
