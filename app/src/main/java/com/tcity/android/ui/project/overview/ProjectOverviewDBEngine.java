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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.R;
import com.tcity.android.app.DB;
import com.tcity.android.concept.ConceptPackage;
import com.tcity.android.concept.Status;
import com.tcity.android.db.DbPackage;
import com.tcity.android.db.Schema;
import com.tcity.android.db.SchemaListener;
import com.tcity.android.ui.adapter.BuildConfigurationAdapter;
import com.tcity.android.ui.adapter.ProjectAdapter;
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
    private final BuildConfigurationClickListener myBuildConfigurationClickListener;

    @NotNull
    private final ProjectClickListener myProjectClickListener;

    @NotNull
    private final BuildConfigurationDBEngine myWatchedBuildConfigurationsEngine;

    @NotNull
    private final BuildConfigurationDBEngine myAllBuildConfigurationsEngine;

    @NotNull
    private final ProjectDBEngine myWatchedProjectsEngine;

    @NotNull
    private final ProjectDBEngine myAllProjectsEngine;

    @NotNull
    private final BuildConfigurationSchemaListener myBuildConfigurationSchemaListener;

    @NotNull
    private final ProjectSchemaListener myProjectSchemaListener;

    ProjectOverviewDBEngine(@NotNull String projectId,
                            @NotNull Context context,
                            @NotNull DB db,
                            @NotNull ViewGroup root) {
        myDB = db;
        myMainAdapter = new MergeAdapter();
        myProjectClickListener = new ProjectClickListener();
        myBuildConfigurationClickListener = new BuildConfigurationClickListener();

        myWatchedBuildConfigurationsEngine = new BuildConfigurationDBEngine(
                context,
                db,
                root,
                myBuildConfigurationClickListener,
                context.getString(R.string.watched) + " " + context.getString(R.string.build_configurations),
                Schema.PARENT_ID_COLUMN + " = ? AND " + Schema.WATCHED_COLUMN + " = ?",
                new String[]{projectId, Integer.toString(DbPackage.getDbValue(true))}
        );

        myAllBuildConfigurationsEngine = new BuildConfigurationDBEngine(
                context,
                db,
                root,
                myBuildConfigurationClickListener,
                context.getString(R.string.build_configurations),
                Schema.PARENT_ID_COLUMN + " = ?",
                new String[]{projectId}
        );

        String projectSectionName = calculateProjectSectionName(projectId, context);

        myWatchedProjectsEngine = new ProjectDBEngine(
                context,
                db,
                root,
                myProjectClickListener,
                context.getString(R.string.watched) + " " + projectSectionName,
                Schema.PARENT_ID_COLUMN + " = ? AND " + Schema.WATCHED_COLUMN + " = ?",
                new String[]{projectId, Integer.toString(DbPackage.getDbValue(true))}
        );

        myAllProjectsEngine = new ProjectDBEngine(
                context,
                db,
                root,
                myProjectClickListener,
                projectSectionName,
                Schema.PARENT_ID_COLUMN + " = ?",
                new String[]{projectId}
        );

        myMainAdapter.addView(myWatchedBuildConfigurationsEngine.getHeader());
        myMainAdapter.addAdapter(myWatchedBuildConfigurationsEngine.getAdapter());

        myMainAdapter.addView(myWatchedProjectsEngine.getHeader());
        myMainAdapter.addAdapter(myWatchedProjectsEngine.getAdapter());

        myMainAdapter.addView(myAllProjectsEngine.getHeader());
        myMainAdapter.addAdapter(myAllProjectsEngine.getAdapter());

        myMainAdapter.addView(myAllBuildConfigurationsEngine.getHeader());
        myMainAdapter.addAdapter(myAllBuildConfigurationsEngine.getAdapter());

        handleHeader(myWatchedBuildConfigurationsEngine);
        handleHeader(myWatchedProjectsEngine);
        handleHeader(myAllProjectsEngine);
        handleHeader(myAllBuildConfigurationsEngine);

        myBuildConfigurationSchemaListener = new BuildConfigurationSchemaListener();
        myProjectSchemaListener = new ProjectSchemaListener();

        myDB.addListener(Schema.PROJECT, myProjectSchemaListener);
        myDB.addListener(Schema.BUILD_CONFIGURATION, myBuildConfigurationSchemaListener);
    }

    @NotNull
    public ListAdapter getAdapter() {
        return myMainAdapter;
    }

    public void setActivity(@Nullable ProjectOverviewActivity activity) {
        myProjectClickListener.myActivity = activity;
        myBuildConfigurationClickListener.myActivity = activity;
    }

    public void projectImageClick(@NotNull String id) {
        ContentValues values = new ContentValues();
        values.putAll(DbPackage.getDbValues(Status.DEFAULT));
        values.putAll(DbPackage.getWatchedDbValues(!isProjectWatched(id)));

        myDB.update(
                Schema.PROJECT,
                values,
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{id}
        );
    }

    public void buildConfigurationImageClick(@NotNull String id) {
        ContentValues values = new ContentValues();
        values.putAll(DbPackage.getDbValues(Status.DEFAULT));
        values.putAll(DbPackage.getWatchedDbValues(!isBuildConfigurationWatched(id)));

        myDB.update(
                Schema.BUILD_CONFIGURATION,
                values,
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{id}
        );
    }

    public void close() {
        myDB.removeListener(Schema.PROJECT, myProjectSchemaListener);
        myDB.removeListener(Schema.BUILD_CONFIGURATION, myBuildConfigurationSchemaListener);

        myWatchedBuildConfigurationsEngine.close();
        myWatchedProjectsEngine.close();
        myAllProjectsEngine.close();
        myAllBuildConfigurationsEngine.close();
    }

    @NotNull
    private String calculateProjectSectionName(@NotNull String projectId,
                                               @NotNull Context context) {
        if (projectId.equals(ConceptPackage.getROOT_PROJECT_ID())) {
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

    private boolean isProjectWatched(@NotNull String id) {
        Cursor cursor = myDB.query(
                Schema.PROJECT,
                new String[]{Schema.WATCHED_COLUMN},
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{id},
                null, null, null, null
        );

        cursor.moveToNext();

        boolean result = DbPackage.getWatched(cursor);

        cursor.close();

        return result;
    }

    private boolean isBuildConfigurationWatched(@NotNull String id) {
        Cursor cursor = myDB.query(
                Schema.BUILD_CONFIGURATION,
                new String[]{Schema.WATCHED_COLUMN},
                Schema.TC_ID_COLUMN + " = ?",
                new String[]{id},
                null, null, null, null
        );

        cursor.moveToNext();

        boolean result = DbPackage.getWatched(cursor);

        cursor.close();

        return result;
    }

    private static class BuildConfigurationClickListener implements BuildConfigurationAdapter.ClickListener {

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

    private static class ProjectClickListener implements ProjectAdapter.ClickListener {

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

    private class BuildConfigurationSchemaListener implements SchemaListener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myWatchedBuildConfigurationsEngine.requery();
                myAllBuildConfigurationsEngine.requery();

                handleHeader(myWatchedBuildConfigurationsEngine);
                handleHeader(myAllBuildConfigurationsEngine);

                myMainAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }

    private class ProjectSchemaListener implements SchemaListener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myWatchedProjectsEngine.requery();
                myAllProjectsEngine.requery();

                handleHeader(myWatchedProjectsEngine);
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
