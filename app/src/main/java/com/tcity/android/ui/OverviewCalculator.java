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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class OverviewCalculator {

    @NotNull
    private final Map<String, View> myProjectViews = new HashMap<>();

    @NotNull
    private final Context myContext;

    @NotNull
    private final LayoutInflater myInflater;

    OverviewCalculator(@NotNull Context context) {
        myContext = context.getApplicationContext();
        myInflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateProjects(@NotNull Collection<Project> projects,
                               @NotNull ViewReceiver receiver) {
        ScrollView overview = (ScrollView) myInflater.inflate(R.layout.overview, null, false);
        LinearLayout parent = (LinearLayout) overview.findViewById(R.id.overview_parent);

        parent.addView(createSeparatorView(R.string.projects, parent));

        for (Project project : projects) {
            parent.addView(createProjectView(project, parent));
        }

        receiver.handleResult(overview);
    }

    @NotNull
    private TextView createSeparatorView(int text, @NotNull ViewGroup parent) {
        TextView result = (TextView) myInflater.inflate(R.layout.separator_item, parent, false);

        result.setText(text);

        return result;
    }

    @NotNull
    private View createProjectView(@NotNull Project project, @NotNull ViewGroup parent) {
        View result = myInflater.inflate(R.layout.concept_item, parent, false);

        TextView name = (TextView) result.findViewById(R.id.concept_item_name);
        name.setText(project.getName());

        ImageButton watch = (ImageButton) result.findViewById(R.id.concept_item_watch);
        watch.setImageResource(android.R.drawable.star_off);

        return result;
    }
}
