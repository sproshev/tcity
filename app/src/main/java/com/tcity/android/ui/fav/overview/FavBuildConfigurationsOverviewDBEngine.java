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

package com.tcity.android.ui.fav.overview;

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
import com.tcity.android.ui.common.overview.BuildConfigurationDBEngine;
import com.tcity.android.ui.common.overview.ConceptClickListener;

import org.jetbrains.annotations.NotNull;

class FavBuildConfigurationsOverviewDBEngine {

    @NotNull
    private final DB myDB;

    @NotNull
    private final MergeAdapter myMainAdapter;

    @NotNull
    private final BuildConfigurationClickListener myClickListener;

    @NotNull
    private final BuildConfigurationDBEngine myEngine;

    @NotNull
    private final BuildConfigurationsListener myDBListener;

    FavBuildConfigurationsOverviewDBEngine(@NotNull Context context,
                                           @NotNull DB db,
                                           @NotNull ViewGroup root) {
        myDB = db;
        myMainAdapter = new MergeAdapter();
        myClickListener = new BuildConfigurationClickListener();

        myEngine = new BuildConfigurationDBEngine(
                null,
                true,
                context,
                db,
                root,
                myClickListener,
                R.string.fav_build_configurations
        );

        myMainAdapter.addView(myEngine.getHeader());
        myMainAdapter.addAdapter(myEngine.getAdapter());

        myDBListener = new BuildConfigurationsListener();

        myDB.addBuildConfigurationsListener(myDBListener);
    }

    @NotNull
    ListAdapter getAdapter() {
        return myMainAdapter;
    }

    void setFragment() {
        // TODO
    }

    void buildConfigurationImageClick(@NotNull String id) {
        myDB.setBuildConfigurationStatus(id, Status.DEFAULT);
        myDB.setFavouriteBuildConfiguration(id, !myDB.isBuildConfigurationFavourite(id));
    }

    void close() {
        myDB.removeBuildConfigurationsListener(myDBListener);

        myEngine.close();
    }

    private class BuildConfigurationClickListener implements ConceptClickListener {

        @Override
        public void onImageClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onDescriptionClick(@NotNull String id) {
            // TODO
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            // TODO
        }
    }

    private class BuildConfigurationsListener implements DB.Listener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myEngine.requery();

                myMainAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }
}
