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

package com.tcity.android.db;

import android.content.Context;
import android.database.Cursor;

import com.tcity.android.Status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DB {

    @NotNull
    private final DBHelper myDBHelper;

    @NotNull
    private final Map<DBHelper.OverviewTable, Collection<Listener>> myListeners;

    public DB(@NotNull Context context) {
        myDBHelper = new DBHelper(context);
        myListeners = new HashMap<>();

        myListeners.put(DBHelper.OverviewTable.PROJECT, new LinkedList<Listener>());
        myListeners.put(DBHelper.OverviewTable.BUILD_CONFIGURATION, new LinkedList<Listener>());
        myListeners.put(DBHelper.OverviewTable.BUILD, new LinkedList<Listener>());
    }

    public void setFavouriteProject(@NotNull String id, boolean favourite) {
        // TODO
    }

    public void setFavouriteBuildConfiguration(@NotNull String id, boolean favourite) {
        // TODO
    }

    public void setFavouriteBuild(@NotNull String id, boolean favourite) {
        // TODO
    }

    public void setProjects() {
        // TODO
    }

    public void setBuildConfigurations(@NotNull String parentProjectId) {
        // TODO
    }

    public void setBuilds(@NotNull String parentBuildConfigurationId) {
        // TODO
    }

    @NotNull
    public Cursor getProjects(@NotNull String parentProjectId, boolean isFavourite) {
        // TODO
    }

    @NotNull
    public Cursor getBuildConfigurations(@NotNull String parentProjectId, boolean isFavourite) {
        // TODO
    }

    @NotNull
    public Cursor getBuilds(@NotNull String parentBuildConfigurationId, boolean isFavourite) {
        // TODO
    }

    public synchronized void addProjectsListener(@NotNull Listener listener) {
        myListeners.get(DBHelper.OverviewTable.PROJECT).add(listener);
    }

    public synchronized void addBuildConfigurationsListener(@NotNull Listener listener) {
        myListeners.get(DBHelper.OverviewTable.BUILD_CONFIGURATION).add(listener);
    }

    public synchronized void addBuildsListener(@NotNull Listener listener) {
        myListeners.get(DBHelper.OverviewTable.BUILD).add(listener);
    }

    public synchronized void removeProjectsListener(@NotNull Listener listener) {
        myListeners.get(DBHelper.OverviewTable.PROJECT).remove(listener);
    }

    public synchronized void removeBuildConfigurationsListener(@NotNull Listener listener) {
        myListeners.get(DBHelper.OverviewTable.BUILD_CONFIGURATION).remove(listener);
    }

    public synchronized void removeBuildsListener(@NotNull Listener listener) {
        myListeners.get(DBHelper.OverviewTable.BUILD).remove(listener);
    }

    public void setProjectStatus(@NotNull String id, @Nullable Status status) {
        // TODO
    }

    public void setBuildConfigurationStatus(@NotNull String id, @Nullable Status status) {
        // TODO
    }

    public static interface Listener {

        public void onChanged();
    }
}
