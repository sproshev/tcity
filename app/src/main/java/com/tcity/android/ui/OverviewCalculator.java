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

package com.tcity.android.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.tcity.android.R;
import com.tcity.android.concept.ConceptPackage;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

class OverviewCalculator {

    @NotNull
    private final LayoutInflater myInflater;

    @NotNull
    private final OverviewCache myCache;

    OverviewCalculator(@NotNull LayoutInflater inflater) {
        myInflater = inflater;
        myCache = new OverviewCache(inflater);
    }

    public void updateProjects(@NotNull Collection<Project> projects,
                               @NotNull Set<String> watchedProjectIds,
                               @NotNull ViewReceiver receiver) {
        ScrollView overview = (ScrollView) myInflater.inflate(R.layout.overview, null, false);
        LinearLayout parent = (LinearLayout) overview.findViewById(R.id.overview_parent);

        int before = parent.getChildCount();
        addWatchedProjects(projects, watchedProjectIds, parent);
        int after = parent.getChildCount();

        if (after - before == 1) {
            parent.removeViewAt(before);
        }

        before = parent.getChildCount();
        addProjects(projects, watchedProjectIds, parent);
        after = parent.getChildCount();

        if (after - before == 1) {
            parent.removeViewAt(before);
        }

        receiver.handleResult(overview);
    }

    private void addWatchedProjects(@NotNull Collection<Project> projects,
                                    @NotNull Set<String> watchedProjectIds,
                                    @NotNull ViewGroup parent) {
        parent.addView(myCache.getSeparatorView(R.string.watched_projects, parent));

        for (Project project : projects) {
            if (!isRoot(project) && watchedProjectIds.contains(project.getId())) {
                parent.addView(myCache.getProjectView(project, watchedProjectIds, parent));
            }
        }
    }

    private boolean isRoot(@NotNull Project project) {
        return project.getId().equals(ConceptPackage.getRootProjectId());
    }

    private void addProjects(@NotNull Collection<Project> projects,
                             @NotNull Set<String> watchedProjectIds,
                             @NotNull ViewGroup parent) {
        parent.addView(myCache.getSeparatorView(R.string.projects, parent));

        for (Project project : projects) {
            if (!isRoot(project)) {
                parent.addView(myCache.getProjectView(project, watchedProjectIds, parent));
            }
        }
    }
}
