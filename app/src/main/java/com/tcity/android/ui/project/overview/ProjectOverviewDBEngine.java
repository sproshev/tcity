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
import android.widget.ListAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.R;
import com.tcity.android.app.DB;
import com.tcity.android.concept.ConceptPackage;
import com.tcity.android.db.DbPackage;
import com.tcity.android.db.Schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectOverviewDBEngine {

    @NotNull
    private final DB myDB;

    @NotNull
    private final MergeAdapter myMainAdapter;

    @NotNull
    private final ClickListener myClickListener;

    @NotNull
    private final ProjectOverviewDBSubEngine myWatchedEngine;

    @NotNull
    private final ProjectOverviewDBSubEngine myAllEngine;

    @NotNull
    private final SchemaListener mySchemaListener;

    ProjectOverviewDBEngine(@NotNull Context context,
                            @NotNull DB db,
                            @NotNull ViewGroup root) {
        myDB = db;
        myMainAdapter = new MergeAdapter();
        myClickListener = new ClickListener();

        myWatchedEngine = new ProjectOverviewDBSubEngine(
                context,
                db,
                root,
                myClickListener,
                context.getString(R.string.watched) + " " + context.getString(R.string.projects),
                DbPackage.calculateSelection(ConceptPackage.getROOT_PROJECT_ID(), Schema.PARENT_ID_COLUMN, Schema.WATCHED_COLUMN),
                DbPackage.calculateSelectionArgs(ConceptPackage.getROOT_PROJECT_ID(), Integer.toString(DbPackage.getDbValue(true)))
        );

        myAllEngine = new ProjectOverviewDBSubEngine(
                context,
                db,
                root,
                myClickListener,
                context.getString(R.string.projects),
                DbPackage.calculateSelection(ConceptPackage.getROOT_PROJECT_ID(), Schema.PARENT_ID_COLUMN),
                DbPackage.calculateSelectionArgs(ConceptPackage.getROOT_PROJECT_ID())
        );

        myMainAdapter.addView(myWatchedEngine.getHeader());
        myMainAdapter.addAdapter(myWatchedEngine.getAdapter());

        myMainAdapter.addView(myAllEngine.getHeader());
        myMainAdapter.addAdapter(myAllEngine.getAdapter());

        handleHeader(myWatchedEngine);
        handleHeader(myAllEngine);

        mySchemaListener = new SchemaListener();

        myDB.addListener(Schema.PROJECT, mySchemaListener);
    }

    @NotNull
    public ListAdapter getAdapter() {
        return myMainAdapter;
    }

    public void setActivity(@Nullable ProjectOverviewActivity activity) {
        myClickListener.myActivity = activity;
    }

    public void close() {
        myDB.removeListener(Schema.PROJECT, mySchemaListener);

        myWatchedEngine.close();
        myAllEngine.close();
    }

    private void handleHeader(@NotNull ProjectOverviewDBSubEngine subEngine) {
        myMainAdapter.setActive(subEngine.getHeader(), !subEngine.empty());
    }

    private static class ClickListener implements ProjectOverviewAdapter.ClickListener {

        @Nullable
        private ProjectOverviewActivity myActivity;

        @Override
        public void onImageClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.onImageClick(id);
            }
        }

        @Override
        public void onNameClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.onNameClick(id);
            }
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            if (myActivity != null) {
                myActivity.onOptionsClick(id, anchor);
            }
        }
    }

    private class SchemaListener implements com.tcity.android.db.SchemaListener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myWatchedEngine.requery();
                myAllEngine.requery();

                handleHeader(myWatchedEngine);
                handleHeader(myAllEngine);

                myMainAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }
}
