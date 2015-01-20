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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.R;
import com.tcity.android.Status;
import com.tcity.android.db.DB;
import com.tcity.android.db.Project;
import com.tcity.android.ui.adapter.BuildConfigurationClickListener;
import com.tcity.android.ui.adapter.ProjectClickListener;
import com.tcity.android.ui.engine.BuildConfigurationDBEngine;
import com.tcity.android.ui.engine.ProjectDBEngine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectOverviewDBEngine {

    @NotNull
    private final DB myDB;

    @NotNull
    private final MergeAdapter myMainAdapter;

    @NotNull
    private final MyBuildConfigurationClickListener myBuildConfigurationClickListener;

    @NotNull
    private final MyProjectClickListener myProjectClickListener;

    @NotNull
    private final BuildConfigurationDBEngine myFavouriteBuildConfigurationsEngine;

    @NotNull
    private final BuildConfigurationDBEngine myAllBuildConfigurationsEngine;

    @NotNull
    private final ProjectDBEngine myFavouriteProjectsEngine;

    @NotNull
    private final ProjectDBEngine myAllProjectsEngine;

    @NotNull
    private final BuildConfigurationsListener myBuildConfigurationsListener;

    @NotNull
    private final ProjectsListener myProjectsListener;

    ProjectOverviewDBEngine(@NotNull String projectId,
                            @NotNull Context context,
                            @NotNull DB db,
                            @NotNull ViewGroup root) {
        myDB = db;
        myMainAdapter = new MergeAdapter();
        myProjectClickListener = new MyProjectClickListener();
        myBuildConfigurationClickListener = new MyBuildConfigurationClickListener();

        myFavouriteBuildConfigurationsEngine = new BuildConfigurationDBEngine(
                projectId,
                true,
                context,
                db,
                root,
                myBuildConfigurationClickListener,
                context.getString(R.string.favourite) + " " + context.getString(R.string.build_configurations)
        );

        myAllBuildConfigurationsEngine = new BuildConfigurationDBEngine(
                projectId,
                false,
                context,
                db,
                root,
                myBuildConfigurationClickListener,
                context.getString(R.string.build_configurations)
        );

        String projectSectionName = calculateProjectSectionName(projectId, context);

        myFavouriteProjectsEngine = new ProjectDBEngine(
                projectId,
                true,
                context,
                db,
                root,
                myProjectClickListener,
                context.getString(R.string.favourite) + " " + projectSectionName
        );

        myAllProjectsEngine = new ProjectDBEngine(
                projectId,
                false,
                context,
                db,
                root,
                myProjectClickListener,
                projectSectionName
        );

        myMainAdapter.addView(myFavouriteBuildConfigurationsEngine.getHeader());
        myMainAdapter.addAdapter(myFavouriteBuildConfigurationsEngine.getAdapter());

        myMainAdapter.addView(myFavouriteProjectsEngine.getHeader());
        myMainAdapter.addAdapter(myFavouriteProjectsEngine.getAdapter());

        myMainAdapter.addView(myAllProjectsEngine.getHeader());
        myMainAdapter.addAdapter(myAllProjectsEngine.getAdapter());

        myMainAdapter.addView(myAllBuildConfigurationsEngine.getHeader());
        myMainAdapter.addAdapter(myAllBuildConfigurationsEngine.getAdapter());

        handleHeader(myFavouriteBuildConfigurationsEngine);
        handleHeader(myFavouriteProjectsEngine);
        handleHeader(myAllProjectsEngine);
        handleHeader(myAllBuildConfigurationsEngine);

        myBuildConfigurationsListener = new BuildConfigurationsListener();
        myProjectsListener = new ProjectsListener();

        myDB.addProjectsListener(myProjectsListener);
        myDB.addBuildConfigurationsListener(myBuildConfigurationsListener);
    }

    @NotNull
    ListAdapter getAdapter() {
        return myMainAdapter;
    }

    void setActivity(@Nullable ProjectOverviewActivity activity) {
        myProjectClickListener.myActivity = activity;
        myBuildConfigurationClickListener.myActivity = activity;
    }

    void projectImageClick(@NotNull String id) {
        myDB.setProjectStatus(id, Status.DEFAULT);
        myDB.setFavouriteProject(id, !myDB.isProjectFavourite(id));
    }

    void buildConfigurationImageClick(@NotNull String id) {
        myDB.setBuildConfigurationStatus(id, Status.DEFAULT);
        myDB.setFavouriteBuildConfiguration(id, !myDB.isBuildConfigurationFavourite(id));

        initSyncLimit(id);
    }

    void close() {
        myDB.removeProjectsListener(myProjectsListener);
        myDB.removeBuildConfigurationsListener(myBuildConfigurationsListener);

        myFavouriteBuildConfigurationsEngine.close();
        myFavouriteProjectsEngine.close();
        myAllProjectsEngine.close();
        myAllBuildConfigurationsEngine.close();
    }

    @NotNull
    private String calculateProjectSectionName(@NotNull String projectId,
                                               @NotNull Context context) {
        if (projectId.equals(Project.ROOT_PROJECT_ID)) {
            return context.getString(R.string.projects);
        } else {
            return context.getString(R.string.subprojects);
        }
    }

    private void handleHeader(@NotNull ProjectDBEngine engine) {
        myMainAdapter.setActive(engine.getHeader(), !engine.empty());
    }

    private void handleHeader(@NotNull BuildConfigurationDBEngine engine) {
        myMainAdapter.setActive(engine.getHeader(), !engine.empty());
    }

    private void initSyncLimit(@NotNull String buildConfigurationId) {
        if (myDB.getBuildConfigurationSyncLimit(buildConfigurationId) == Long.MIN_VALUE) {
            myDB.setBuildConfigurationSyncLimit(
                    buildConfigurationId,
                    System.currentTimeMillis()
            );
        }
    }

    private static class MyBuildConfigurationClickListener implements BuildConfigurationClickListener {

        @Nullable
        private ProjectOverviewActivity myActivity;

        @Override
        public void onImageClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.buildConfigurationImageClick(id);
            }
        }

        @Override
        public void onNameClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.buildConfigurationNameClick(id);
            }
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            if (myActivity != null) {
                myActivity.buildConfigurationOptionsClick(id, anchor);
            }
        }
    }

    private static class MyProjectClickListener implements ProjectClickListener {

        @Nullable
        private ProjectOverviewActivity myActivity;

        @Override
        public void onImageClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.projectImageClick(id);
            }
        }

        @Override
        public void onNameClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.projectNameClick(id);
            }
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            if (myActivity != null) {
                myActivity.projectOptionsClick(id, anchor);
            }
        }
    }

    private class BuildConfigurationsListener implements DB.Listener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myFavouriteBuildConfigurationsEngine.requery();
                myAllBuildConfigurationsEngine.requery();

                handleHeader(myFavouriteBuildConfigurationsEngine);
                handleHeader(myAllBuildConfigurationsEngine);

                myMainAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }

    private class ProjectsListener implements DB.Listener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myFavouriteProjectsEngine.requery();
                myAllProjectsEngine.requery();

                handleHeader(myFavouriteProjectsEngine);
                handleHeader(myAllProjectsEngine);

                myMainAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }
}
