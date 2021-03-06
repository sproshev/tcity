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

package com.tcity.android.ui.overview.buildconfiguration;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.db.DB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildConfigurationOverviewEngine {

    @NotNull
    private final BuildConfigurationOverviewDBEngine myDBEngine;

    @NotNull
    private final BuildConfigurationOverviewServerEngine myServerEngine;

    BuildConfigurationOverviewEngine(@NotNull String buildConfigurationId,
                                     @NotNull Context context,
                                     @NotNull DB db,
                                     @NotNull ViewGroup root) {
        myDBEngine = new BuildConfigurationOverviewDBEngine(buildConfigurationId, context, db, root);

        myServerEngine = new BuildConfigurationOverviewServerEngine(
                buildConfigurationId,
                db,
                new RestClient(new Preferences(context))
        );
    }

    @NotNull
    ListAdapter getAdapter() {
        return myDBEngine.getAdapter();
    }

    void setActivity(@Nullable BuildConfigurationOverviewActivity activity) {
        myDBEngine.setActivity(activity);
        myServerEngine.setActivity(activity);
    }

    boolean isRefreshing() {
        return myServerEngine.isRefreshing();
    }

    @Nullable
    Exception getException() {
        return myServerEngine.getException();
    }

    void resetException() {
        myServerEngine.resetException();
    }

    void imageClick(@NotNull String id) {
        myDBEngine.imageClick(id);
    }

    void refresh(boolean force) {
        myServerEngine.refresh(force);
    }

    void close() {
        myDBEngine.close();
    }
}
