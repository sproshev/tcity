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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class OverviewCache {

    private static final int WATCHED_IMAGE = android.R.drawable.star_on;

    private static final int UNWATCHED_IMAGE = android.R.drawable.star_off;

    @NotNull
    private final LayoutInflater myInflater;

    @NotNull
    private final Map<Integer, TextView> mySeparators = new HashMap<>();

    @NotNull
    private final Map<String, View> myProjects = new HashMap<>();

    public OverviewCache(@NotNull LayoutInflater inflater) {
        myInflater = inflater;
    }

    @NotNull
    public View getSeparatorView(int text, @NotNull ViewGroup parent) {
        if (!mySeparators.containsKey(text)) {
            mySeparators.put(
                    text,
                    createSeparatorView(text, parent)
            );
        }

        return mySeparators.get(text);
    }

    @NotNull
    public View getProjectView(@NotNull Project project,
                               @NotNull Set<String> watchedProjectIds,
                               @NotNull ViewGroup parent) {
        String projectId = project.getId();

        if (!myProjects.containsKey(projectId)) {
            myProjects.put(
                    projectId,
                    createProjectView(project, watchedProjectIds, parent)
            );
        }

        return myProjects.get(projectId);
    }

    @NotNull
    private TextView createSeparatorView(int text, @NotNull ViewGroup parent) {
        TextView result = (TextView) myInflater.inflate(R.layout.separator_item, parent, false);

        result.setText(text);

        return result;
    }

    @NotNull
    private View createProjectView(@NotNull Project project,
                                   @NotNull Set<String> watchedProjectIds,
                                   @NotNull ViewGroup parent) {
        View result = myInflater.inflate(R.layout.concept_item, parent, false);

        TextView name = (TextView) result.findViewById(R.id.concept_item_name);
        name.setText(project.getName());

        int image = watchedProjectIds.contains(project.getId()) ? WATCHED_IMAGE : UNWATCHED_IMAGE;

        ImageButton watch = (ImageButton) result.findViewById(R.id.concept_item_watch);
        watch.setImageResource(image);

        return result;
    }
}
