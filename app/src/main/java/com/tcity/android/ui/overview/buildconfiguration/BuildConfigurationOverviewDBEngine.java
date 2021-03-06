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
import com.tcity.android.R;
import com.tcity.android.db.DB;
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
    private final BuildDBEngine myFavouriteEngine;

    @NotNull
    private final BuildDBEngine myAllEngine;

    @NotNull
    private final DB.Listener myDBListener;

    BuildConfigurationOverviewDBEngine(@NotNull String buildConfigurationId,
                                       @NotNull Context context,
                                       @NotNull DB db,
                                       @NotNull ViewGroup root) {
        myDB = db;
        myMainAdapter = new MergeAdapter();
        myClickListener = new MyBuildClickListener();

        myFavouriteEngine = new BuildDBEngine(
                buildConfigurationId,
                true,
                context,
                db,
                root,
                myClickListener,
                context.getString(R.string.favourite) + " " + context.getString(R.string.builds)
        );

        myAllEngine = new BuildDBEngine(
                buildConfigurationId,
                false,
                context,
                db,
                root,
                myClickListener,
                context.getString(R.string.builds)
        );

        myMainAdapter.addView(myFavouriteEngine.getHeader());
        myMainAdapter.addAdapter(myFavouriteEngine.getAdapter());

        myMainAdapter.addView(myAllEngine.getHeader());
        myMainAdapter.addAdapter(myAllEngine.getAdapter());

        handleHeaders();

        myDBListener = new MySchemaListener();

        myDB.addBuildsListener(myDBListener);
    }

    @NotNull
    ListAdapter getAdapter() {
        return myMainAdapter;
    }

    void setActivity(@Nullable BuildConfigurationOverviewActivity activity) {
        myClickListener.myActivity = activity;
    }

    void imageClick(@NotNull String id) {
        myDB.setFavouriteBuild(id, !myDB.isBuildFavourite(id));
    }

    void close() {
        myDB.removeBuildsListener(myDBListener);

        myFavouriteEngine.close();
        myAllEngine.close();
    }

    private void handleHeaders() {
        myMainAdapter.setActive(myFavouriteEngine.getHeader(), !myFavouriteEngine.empty());
        myMainAdapter.setActive(myAllEngine.getHeader(), !myAllEngine.empty());
    }

    private static class MyBuildClickListener implements BuildClickListener {

        @Nullable
        private BuildConfigurationOverviewActivity myActivity;

        @Override
        public void onImageClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.imageClick(id);
            }
        }

        @Override
        public void onDescriptionClick(@NotNull String id) {
            if (myActivity != null) {
                myActivity.nameClick(id);
            }
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            if (myActivity != null) {
                myActivity.optionsClick(id, anchor);
            }
        }
    }

    private class MySchemaListener implements DB.Listener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myFavouriteEngine.requery();
                myAllEngine.requery();

                handleHeaders();

                myMainAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }
}
