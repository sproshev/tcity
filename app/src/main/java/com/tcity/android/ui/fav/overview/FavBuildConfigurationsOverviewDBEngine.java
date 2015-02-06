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
import android.widget.BaseAdapter;

import com.tcity.android.Status;
import com.tcity.android.db.DB;
import com.tcity.android.ui.common.overview.ConceptClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FavBuildConfigurationsOverviewDBEngine {

    @NotNull
    private final DB myDb;

    @NotNull
    private final BuildConfigurationClickListener myClickListener;

    @NotNull
    private final FavBuildConfigurationDBEngine myEngine;

    @NotNull
    private final BuildConfigurationsListener myDBListener;

    FavBuildConfigurationsOverviewDBEngine(@NotNull Context context,
                                           @NotNull DB db) {
        myDb = db;
        myClickListener = new BuildConfigurationClickListener();

        myEngine = new FavBuildConfigurationDBEngine(
                context,
                db,
                myClickListener
        );

        myDBListener = new BuildConfigurationsListener();

        myDb.addBuildConfigurationsListener(myDBListener);
    }

    @NotNull
    BaseAdapter getAdapter() {
        return myEngine.getAdapter();
    }

    void setFragment(@Nullable FavBuildConfigurationsOverviewFragment fragment) {
        myClickListener.myFragment = fragment;
    }

    void imageClick(@NotNull String id) {
        myDb.setBuildConfigurationStatus(id, Status.DEFAULT);
        myDb.setFavouriteBuildConfiguration(id, !myDb.isBuildConfigurationFavourite(id));
    }

    void close() {
        myDb.removeBuildConfigurationsListener(myDBListener);

        myEngine.close();
    }

    private class BuildConfigurationClickListener implements ConceptClickListener {

        @Nullable
        private FavBuildConfigurationsOverviewFragment myFragment;

        @Override
        public void onImageClick(@NotNull String id) {
            if (myFragment != null) {
                myFragment.imageClick(id);
            }
        }

        @Override
        public void onDescriptionClick(@NotNull String id) {
            if (myFragment != null) {
                myFragment.descriptionClick(id);
            }
        }

        @Override
        public void onOptionsClick(@NotNull String id, @NotNull View anchor) {
            if (myFragment != null) {
                myFragment.optionsClick(id, anchor);
            }
        }
    }

    private class BuildConfigurationsListener implements DB.Listener {

        @NotNull
        private final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);

                myEngine.requery();

                myEngine.getAdapter().notifyDataSetChanged();
            }
        };

        @Override
        public void onChanged() {
            myHandler.sendEmptyMessage(0);
        }
    }
}
