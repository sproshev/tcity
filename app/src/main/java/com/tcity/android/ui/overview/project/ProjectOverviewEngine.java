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

package com.tcity.android.ui.overview.project;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.tcity.android.app.DB;
import com.tcity.android.app.Preferences;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectOverviewEngine {

    @NotNull
    private final ProjectOverviewDBEngine myDBEngine;

    @NotNull
    private final ProjectOverviewServerEngine myServerEngine;

    ProjectOverviewEngine(@NotNull String projectId,
                          @NotNull Context context,
                          @NotNull DB db,
                          @NotNull ViewGroup root) {
        myDBEngine = new ProjectOverviewDBEngine(projectId, context, db, root);
        myServerEngine = new ProjectOverviewServerEngine(projectId, new Preferences(context), db);
    }

    @NotNull
    public ListAdapter getAdapter() {
        return myDBEngine.getAdapter();
    }

    public void setActivity(@Nullable ProjectOverviewActivity activity) {
        myDBEngine.setActivity(activity);
        myServerEngine.setActivity(activity);
    }

    public void projectImageClick(@NotNull String id) {
        myDBEngine.projectImageClick(id);
        myServerEngine.projectImageClick(id);
    }

    public void buildConfigurationImageClick(@NotNull String id) {
        myDBEngine.buildConfigurationImageClick(id);
        myServerEngine.buildConfigurationImageClick(id);
    }

    public void refresh() {
        myServerEngine.refresh();
    }

    public void close() {
        myDBEngine.close();
    }
}
