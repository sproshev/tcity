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

import com.tcity.android.app.DB;

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
        myServerEngine = new BuildConfigurationOverviewServerEngine();
    }

    @NotNull
    public ListAdapter getAdapter() {
        return myDBEngine.getAdapter();
    }

    public void setActivity(@Nullable BuildConfigurationOverviewActivity activity) {
        myDBEngine.setActivity(activity);
        myServerEngine.setActivity(activity);
    }

    public void refresh() {
        myServerEngine.refresh();
    }

    public void close() {
        myDBEngine.close();
    }
}
