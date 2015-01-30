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

import com.tcity.android.background.rest.RestClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class BuildArtifactsTask extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final String myBuildId;

    @Nullable
    private final BuildArtifact.Type myType;

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
                       @Nullable BuildArtifact.Type type,
                       @Nullable String path,
                       @NotNull RestClient client) {
        myBuildId = buildId;
        myType = type;
        myPath = path;
        myClient = client;
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        myResult = new ArrayList<>();

        myResult.add(new BuildArtifact(100, "abc", BuildArtifact.Type.DIR));
        myResult.add(new BuildArtifact(200, "def", BuildArtifact.Type.ARCHIVE));
        myResult.add(new BuildArtifact(300, "ghi", BuildArtifact.Type.FILE));

        // TODO

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
