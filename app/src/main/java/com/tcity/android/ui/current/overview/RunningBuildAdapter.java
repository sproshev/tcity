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

package com.tcity.android.ui.current.overview;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.db.DB;
import com.tcity.android.obj.Project;
import com.tcity.android.obj.RunningBuild;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class RunningBuildAdapter extends BaseAdapter {

    @NotNull
    private final LayoutInflater myInflater;

    @NotNull
    private final RunningBuildListener myListener;

    @NotNull
    private final DB myDb;

    @NotNull
    private List<RunningBuild> myData = Collections.emptyList();

    RunningBuildAdapter(@NotNull Context context,
                        @NotNull RunningBuildListener listener,
                        @NotNull DB db) {
        myInflater = LayoutInflater.from(context);
        myListener = listener;
        myDb = db;
    }

    void setData(@NotNull List<RunningBuild> data) {
        myData = data;
    }

    @Override
    public int getCount() {
        return myData.size();
    }

    @NotNull
    @Override
    public RunningBuild getItem(int position) {
        return myData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NotNull
    @Override
    public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.running_build_item, parent, false);

            convertView.setTag(
                    new ViewHolder(
                            (LinearLayout) convertView.findViewById(R.id.running_build_description_layout),
                            (TextView) convertView.findViewById(R.id.running_build_projects),
                            (TextView) convertView.findViewById(R.id.running_build_percent),
                            (TextView) convertView.findViewById(R.id.running_build_build_configuration),
                            (TextView) convertView.findViewById(R.id.running_build_name),
                            (TextView) convertView.findViewById(R.id.running_build_branch),
                            convertView.findViewById(R.id.running_build_options)
                    )
            );
        }

        RunningBuild runningBuild = getItem(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();

        bindDescription(holder.description, runningBuild);
        bindOptions(holder.options, runningBuild);
        bindProjects(holder.projects, runningBuild.parentBuildConfigurationId);
        bindBranch(holder.branch, runningBuild.branch, runningBuild.isBranchDefault);

        holder.percent.setText(runningBuild.percentageComplete + "%");
        holder.name.setText(runningBuild.name);
        holder.buildConfiguration.setText(
                myDb.getBuildConfigurationName(runningBuild.parentBuildConfigurationId)
        );

        return convertView;
    }

    private void bindDescription(@NotNull LinearLayout view, @NotNull RunningBuild runningBuild) {
        final String id = runningBuild.id;

        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myListener.onDescriptionClick(id);
                    }
                }
        );
    }

    private void bindOptions(@NotNull View view, @NotNull RunningBuild runningBuild) {
        final String id = runningBuild.id;

        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myListener.onOptionsClick(id, v);
                    }
                }
        );
    }

    private void bindProjects(@NotNull TextView view, @NotNull String buildConfigurationId) {
        StringBuilder sb = new StringBuilder();

        for (String name : calculateProjectNames(myDb.getBuildConfigurationParentId(buildConfigurationId))) {
            sb.append(name).append(" :: ");
        }

        sb.setLength(sb.length() - 4);

        view.setText(sb.toString());
    }

    private void bindBranch(@NotNull TextView view,
                            @Nullable String branch,
                            boolean isBranchDefault) {
        if (branch == null) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(branch);

            if (isBranchDefault) {
                view.setTypeface(null, Typeface.BOLD);
            } else {
                view.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    @NotNull
    private LinkedList<String> calculateProjectNames(@NotNull String projectId) {
        LinkedList<String> names = new LinkedList<>();

        names.addFirst(myDb.getProjectName(projectId));

        String grandProjectId = myDb.getProjectParentId(projectId);

        while (!grandProjectId.equals(Project.ROOT_PROJECT_ID)) {
            names.addFirst(myDb.getProjectName(grandProjectId));

            grandProjectId = myDb.getProjectParentId(grandProjectId);
        }

        return names;
    }

    private static class ViewHolder {

        @NotNull
        private final LinearLayout description;

        @NotNull
        private final TextView projects;

        @NotNull
        private final TextView percent;

        @NotNull
        private final TextView buildConfiguration;

        @NotNull
        private final TextView name;

        @NotNull
        private final TextView branch;

        @NotNull
        private final View options;

        private ViewHolder(@NotNull LinearLayout description,
                           @NotNull TextView projects,
                           @NotNull TextView percent,
                           @NotNull TextView buildConfiguration,
                           @NotNull TextView name,
                           @NotNull TextView branch,
                           @NotNull View options) {
            this.description = description;
            this.projects = projects;
            this.percent = percent;
            this.buildConfiguration = buildConfiguration;
            this.name = name;
            this.branch = branch;
            this.options = options;
        }
    }
}
