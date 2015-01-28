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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class BuildInfoTask extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final String myBuildId;

    @NotNull
    private final BuildInfoFragment myFragment;

    @Nullable
    private Exception myException;

    @Nullable
    private Map<String, String> myResult;

    BuildInfoTask(@NotNull String buildId,
                  @NotNull BuildInfoFragment fragment) {
        myBuildId = buildId;
        myFragment = fragment;
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        myResult = new HashMap<>();
        myResult.put("k1", "v1");
        myResult.put("k2", "v2");
        myResult.put("k3", "v3");
        myResult.put("k4", "v4");
        myResult.put("k5", "v5");
        myResult.put("k6", "v6");
        myResult.put("k7", "v7");
        myResult.put("k8", "v8");
        myResult.put("k9", "v9");
        myResult.put("k10", "v10");

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
}
