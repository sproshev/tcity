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

package com.tcity.android.ui.buildconfiguration.overview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.R;
import com.tcity.android.db.DB;
import com.tcity.android.ui.common.overview.ConceptClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildConfigurationOverviewDBEngine {

    @NotNull
    private final DB myDb;

    @NotNull
    private final MergeAdapter myMainAdapter;

    @NotNull
    private final BuildClickListener myClickListener;

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
        myDb = db;
        myMainAdapter = new MergeAdapter();
        myClickListener = new BuildClickListener();

        myFavouriteEngine = new BuildDBEngine(
                buildConfigurationId,
                true,
                context,
                db,
                root,
                myClickListener,
                R.string.fav_builds
        );

        myAllEngine = new BuildDBEngine(
                buildConfigurationId,
                false,
                context,
                db,
                root,
                myClickListener,
                R.string.builds
        );

        myMainAdapter.addView(myFavouriteEngine.getHeader());
        myMainAdapter.addAdapter(myFavouriteEngine.getAdapter());

        myMainAdapter.addView(myAllEngine.getHeader());
        myMainAdapter.addAdapter(myAllEngine.getAdapter());

        handleHeaders();

        myDBListener = new MySchemaListener();

        myDb.addBuildsListener(myDBListener);
    }

    @NotNull
    BaseAdapter getAdapter() {
        return myMainAdapter;
    }

    void setActivity(@Nullable BuildConfigurationOverviewActivity activity) {
        myClickListener.myActivity = activity;
    }

    void imageClick(@NotNull String id) {
        myDb.setFavouriteBuild(id, !myDb.isBuildFavourite(id));
    }

    void close() {
        myDb.removeBuildsListener(myDBListener);

        myFavouriteEngine.close();
        myAllEngine.close();
    }

    private void handleHeaders() {
        myMainAdapter.setActive(myFavouriteEngine.getHeader(), !myFavouriteEngine.empty());
        myMainAdapter.setActive(myAllEngine.getHeader(), !myAllEngine.empty());
    }

    private static class BuildClickListener implements ConceptClickListener {

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
                myActivity.descriptionClick(id);
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