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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.app.DB;
import com.tcity.android.db.Schema;
import com.tcity.android.db.SchemaListener;
import com.tcity.android.ui.adapter.BuildClickListener;
import com.tcity.android.ui.engine.BuildDBEngine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildConfigurationOverviewDBEngine {

    @NotNull
    private final DB myDB;

    @NotNull
    private final MergeAdapter myMainAdapter;

    @NotNull
    private final MyBuildClickListener myClickListener;

    @NotNull
    private final BuildDBEngine myEngine;

    @NotNull
    private final SchemaListener mySchemaListener;

    BuildConfigurationOverviewDBEngine(@NotNull String buildConfigurationId,
                                       @NotNull Context context,
                                       @NotNull DB db,
                                       @NotNull ViewGroup root) {
        myDB = db;
        myMainAdapter = new MergeAdapter();
        myClickListener = new MyBuildClickListener();

        myEngine = new BuildDBEngine(
                context,
                db,
                root,
                myClickListener,
                Schema.PARENT_ID_COLUMN + " = ?",
                new String[]{buildConfigurationId}
        );

        myMainAdapter.addView(myEngine.getHeader());
        myMainAdapter.addAdapter(myEngine.getAdapter());

        handleHeader();

        mySchemaListener = new MySchemaListener();

        myDB.addListener(Schema.BUILD, mySchemaListener);
    }

    @NotNull
    public ListAdapter getAdapter() {
        return myMainAdapter;
    }

    public void setActivity(@Nullable BuildConfigurationOverviewActivity activity) {
        myClickListener.myActivity = activity;
    }

    public void close() {
        myDB.removeListener(Schema.BUILD, mySchemaListener);

        myEngine.close();
    }

    private void handleHeader() {
        myMainAdapter.setActive(myEngine.getHeader(), !myEngine.empty());
    }

    private static class MyBuildClickListener implements BuildClickListener {

        @Nullable
        private BuildConfigurationOverviewActivity myActivity;

        @Override
        public void onNameClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            // TODO
        }
    }

    private class MySchemaListener implements SchemaListener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myEngine.requery();

                handleHeader();

                myMainAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }
}
