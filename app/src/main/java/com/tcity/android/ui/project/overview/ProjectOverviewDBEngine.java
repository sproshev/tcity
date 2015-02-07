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

package com.tcity.android.ui.project.overview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.R;
import com.tcity.android.Status;
import com.tcity.android.db.DB;
import com.tcity.android.obj.Project;
import com.tcity.android.ui.common.overview.ConceptClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectOverviewDBEngine {

    @NotNull
    private final DB myDb;

    @NotNull
    private final MergeAdapter myMainAdapter;

    @NotNull
    private final BuildConfigurationClickListener myBuildConfigurationClickListener;

    @NotNull
    private final ProjectClickListener myProjectClickListener;

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
        myDb = db;
        myMainAdapter = new MergeAdapter();
        myProjectClickListener = new ProjectClickListener();
        myBuildConfigurationClickListener = new BuildConfigurationClickListener();

        myFavouriteBuildConfigurationsEngine = new BuildConfigurationDBEngine(
                projectId,
                true,
                context,
                db,
                root,
                myBuildConfigurationClickListener,
                R.string.fav_build_configurations
        );

        myAllBuildConfigurationsEngine = new BuildConfigurationDBEngine(
                projectId,
                false,
                context,
                db,
                root,
                myBuildConfigurationClickListener,
                R.string.build_configurations
        );

        boolean isRootProject = projectId.equals(Project.ROOT_PROJECT_ID);

        myFavouriteProjectsEngine = new ProjectDBEngine(
                projectId,
                true,
                context,
                db,
                root,
                myProjectClickListener,
                (isRootProject ? R.string.fav_projects : R.string.fav_subprojects)
        );

        myAllProjectsEngine = new ProjectDBEngine(
                projectId,
                false,
                context,
                db,
                root,
                myProjectClickListener,
                (isRootProject ? R.string.projects : R.string.subprojects)
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

        myDb.addProjectsListener(myProjectsListener);
        myDb.addBuildConfigurationsListener(myBuildConfigurationsListener);
    }

    @NotNull
    BaseAdapter getAdapter() {
        return myMainAdapter;
    }

    void setListener(@Nullable ProjectOverviewListener listener) {
        myProjectClickListener.myListener = listener;
        myBuildConfigurationClickListener.myListener = listener;
    }

    void projectImageClick(@NotNull String id) {
        myDb.setProjectStatus(id, Status.DEFAULT);
        myDb.setFavouriteProject(id, !myDb.isProjectFavourite(id));
    }

    void buildConfigurationImageClick(@NotNull String id) {
        myDb.setBuildConfigurationStatus(id, Status.DEFAULT);
        myDb.setFavouriteBuildConfiguration(id, !myDb.isBuildConfigurationFavourite(id));
    }

    void close() {
        myDb.removeProjectsListener(myProjectsListener);
        myDb.removeBuildConfigurationsListener(myBuildConfigurationsListener);

        myFavouriteBuildConfigurationsEngine.close();
        myFavouriteProjectsEngine.close();
        myAllProjectsEngine.close();
        myAllBuildConfigurationsEngine.close();
    }

    private void handleHeader(@NotNull ProjectDBEngine engine) {
        myMainAdapter.setActive(engine.getHeader(), !engine.empty());
    }

    private void handleHeader(@NotNull BuildConfigurationDBEngine engine) {
        myMainAdapter.setActive(engine.getHeader(), !engine.empty());
    }

    private static class BuildConfigurationClickListener implements ConceptClickListener {

        @Nullable
        private ProjectOverviewListener myListener;

        @Override
        public void onImageClick(@NotNull String id) {
            if (myListener != null) {
                myListener.buildConfigurationImageClick(id);
            }
        }

        @Override
        public void onDescriptionClick(@NotNull String id) {
            if (myListener != null) {
                myListener.buildConfigurationDescriptionClick(id);
            }
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            if (myListener != null) {
                myListener.buildConfigurationOptionsClick(id, anchor);
            }
        }
    }

    private static class ProjectClickListener implements ConceptClickListener {

        @Nullable
        private ProjectOverviewListener myListener;

        @Override
        public void onImageClick(@NotNull String id) {
            if (myListener != null) {
                myListener.projectImageClick(id);
            }
        }

        @Override
        public void onDescriptionClick(@NotNull String id) {
            if (myListener != null) {
                myListener.projectDescriptionClick(id);
            }
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            if (myListener != null) {
                myListener.projectOptionsClick(id, anchor);
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
