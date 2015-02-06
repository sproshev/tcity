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
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.db.DB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FavBuildConfigurationsOverviewEngine {

    @NotNull
    private final FavBuildConfigurationsOverviewDBEngine myDBEngine;

    @NotNull
    private final FavBuildConfigurationsOverviewServerEngine myServerEngine;

    FavBuildConfigurationsOverviewEngine(@NotNull Context context,
                                         @NotNull DB db,
                                         @NotNull ViewGroup root) {
        myDBEngine = new FavBuildConfigurationsOverviewDBEngine(context, db, root);

        myServerEngine = new FavBuildConfigurationsOverviewServerEngine(
                db,
                new RestClient(new Preferences(context))
        );
    }

    @NotNull
    ListAdapter getAdapter() {
        return myDBEngine.getAdapter();
    }

    void setFragment() {
        myDBEngine.setFragment();
        myServerEngine.setFragment();
    }

    boolean isRefreshing() {
        return myServerEngine.isRefreshing();
    }

    @Nullable
    Exception getException() {
        return myServerEngine.getException();
    }

    void resetException() {
        myServerEngine.resetException();
    }

    void buildConfigurationImageClick(@NotNull String id) {
        myDBEngine.buildConfigurationImageClick(id);
    }

    void refresh(boolean force) {
        myServerEngine.refresh(force);
    }

    void close() {
        myDBEngine.close();
    }
}
