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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tcity.android.Status;
import com.tcity.android.background.parser.Build;
import com.tcity.android.background.parser.BuildConfiguration;
import com.tcity.android.background.parser.Project;

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
    private final Map<OverviewTable, Collection<Listener>> myListeners;

    public DB(@NotNull Context context) {
        myDBHelper = new DBHelper(context);
        myListeners = new HashMap<>();

        myListeners.put(OverviewTable.PROJECT, new LinkedList<Listener>());
        myListeners.put(OverviewTable.BUILD_CONFIGURATION, new LinkedList<Listener>());
        myListeners.put(OverviewTable.BUILD, new LinkedList<Listener>());
    }

    public void setFavouriteProject(@NotNull String id, boolean favourite) {
        setFavourite(FavouriteTable.PROJECT, id, favourite);

        notifyListeners(OverviewTable.PROJECT);
    }

    public void setFavouriteBuildConfiguration(@NotNull String id, boolean favourite) {
        setFavourite(FavouriteTable.BUILD_CONFIGURATION, id, favourite);

        notifyListeners(OverviewTable.BUILD_CONFIGURATION);
    }

    public void setFavouriteBuild(@NotNull String id, boolean favourite) {
        setFavourite(FavouriteTable.BUILD, id, favourite);

        notifyListeners(OverviewTable.BUILD);
    }

    public void setProjects(@NotNull Collection<Project> projects) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(OverviewTable.PROJECT.getName(), null, null);

            for (Project project : projects) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), project.getId());
                values.put(Column.PARENT_ID.getName(), project.getParentId());
                values.put(Column.NAME.getName(), project.getName());

                db.insert(OverviewTable.PROJECT.getName(), null, values);
            }

            db.setTransactionSuccessful();

            notifyListeners(OverviewTable.PROJECT);
        } finally {
            db.endTransaction();
        }
    }

    public void setBuildConfigurations(@NotNull String parentProjectId,
                                       @NotNull Collection<BuildConfiguration> buildConfigurations) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    OverviewTable.BUILD_CONFIGURATION.getName(),
                    Column.PARENT_ID.getName() + " = ?",
                    new String[]{parentProjectId}
            );

            for (BuildConfiguration buildConfiguration : buildConfigurations) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), buildConfiguration.getId());
                values.put(Column.PARENT_ID.getName(), buildConfiguration.getParentId());
                values.put(Column.NAME.getName(), buildConfiguration.getName());

                db.insert(OverviewTable.BUILD_CONFIGURATION.getName(), null, values);
            }

            db.setTransactionSuccessful();

            notifyListeners(OverviewTable.BUILD_CONFIGURATION);
        } finally {
            db.endTransaction();
        }
    }

    public void setBuilds(@NotNull String parentBuildConfigurationId,
                          @NotNull Collection<Build> builds) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    OverviewTable.BUILD.getName(),
                    Column.PARENT_ID.getName() + " = ?",
                    new String[]{parentBuildConfigurationId}
            );

            for (Build build : builds) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), build.getId());
                values.put(Column.PARENT_ID.getName(), build.getParentId());
                values.put(Column.NAME.getName(), build.getName());

                db.insert(OverviewTable.BUILD.getName(), null, values);
            }

            db.setTransactionSuccessful();

            notifyListeners(OverviewTable.BUILD);
        } finally {
            db.endTransaction();
        }
    }

    @NotNull
    public Cursor getProjects(@NotNull String parentProjectId, boolean onlyFavourite) {
        String mainTable = OverviewTable.PROJECT.getName();
        String favouriteTable = FavouriteTable.PROJECT.getName();
        String statusTable = StatusTable.PROJECT.getName();

        return getProjectsOrBuildConfigurations(
                mainTable,
                favouriteTable,
                statusTable,
                parentProjectId,
                onlyFavourite
        );
    }

    @NotNull
    public Cursor getBuildConfigurations(@NotNull String parentProjectId, boolean onlyFavourite) {
        String mainTable = OverviewTable.BUILD_CONFIGURATION.getName();
        String favouriteTable = FavouriteTable.BUILD_CONFIGURATION.getName();
        String statusTable = StatusTable.BUILD_CONFIGURATION.getName();

        return getProjectsOrBuildConfigurations(
                mainTable,
                favouriteTable,
                statusTable,
                parentProjectId,
                onlyFavourite
        );
    }

    @NotNull
    public Cursor getBuilds(@NotNull String parentBuildConfigurationId, boolean onlyFavourite) {
        String mainTable = OverviewTable.BUILD.getName();
        String favouriteTable = FavouriteTable.BUILD.getName();

        String idColumn = Column.TC_ID.getName();

        if (onlyFavourite) {
            return myDBHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM " + mainTable +
                            " INNER JOIN " + favouriteTable +
                            " ON " + mainTable + "." + idColumn + " = " + favouriteTable + "." + idColumn +
                            " AND " + favouriteTable + "." + Column.FAVOURITE.getName() + " = true" +
                            " WHERE " + mainTable + "." + Column.PARENT_ID.getName() + " = " + parentBuildConfigurationId,
                    new String[]{}
            );
        } else {
            return myDBHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM " + mainTable +
                            " LEFT JOIN " + favouriteTable +
                            " ON " + mainTable + "." + idColumn + " = " + favouriteTable + "." + idColumn +
                            " WHERE " + mainTable + "." + Column.PARENT_ID.getName() + " = " + parentBuildConfigurationId,
                    new String[]{}
            );
        }
    }

    public synchronized void addProjectsListener(@NotNull Listener listener) {
        myListeners.get(OverviewTable.PROJECT).add(listener);
    }

    public synchronized void addBuildConfigurationsListener(@NotNull Listener listener) {
        myListeners.get(OverviewTable.BUILD_CONFIGURATION).add(listener);
    }

    public synchronized void addBuildsListener(@NotNull Listener listener) {
        myListeners.get(OverviewTable.BUILD).add(listener);
    }

    public synchronized void removeProjectsListener(@NotNull Listener listener) {
        myListeners.get(OverviewTable.PROJECT).remove(listener);
    }

    public synchronized void removeBuildConfigurationsListener(@NotNull Listener listener) {
        myListeners.get(OverviewTable.BUILD_CONFIGURATION).remove(listener);
    }

    public synchronized void removeBuildsListener(@NotNull Listener listener) {
        myListeners.get(OverviewTable.BUILD).remove(listener);
    }

    public void setProjectStatus(@NotNull String id, @Nullable Status status) {
        setProjectOrBuildConfigurationStatus(StatusTable.PROJECT, id, status);

        notifyListeners(OverviewTable.PROJECT);
    }

    public void setBuildConfigurationStatus(@NotNull String id, @Nullable Status status) {
        setProjectOrBuildConfigurationStatus(StatusTable.BUILD_CONFIGURATION, id, status);

        notifyListeners(OverviewTable.BUILD_CONFIGURATION);
    }

    private void setFavourite(@NotNull FavouriteTable table,
                              @NotNull String id,
                              boolean favourite) {
        if (!favourite) {
            myDBHelper.getWritableDatabase().delete(
                    table.getName(),
                    Column.TC_ID + " = ?",
                    new String[]{id}
            );
        } else {
            Cursor cursor = myDBHelper.getReadableDatabase().query(
                    table.getName(),
                    null,
                    Column.TC_ID.getName() + " = ?",
                    new String[]{id},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), id);
                values.put(Column.FAVOURITE.getName(), true);

                myDBHelper.getWritableDatabase().insert(table.getName(), null, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(Column.FAVOURITE.getName(), true);

                myDBHelper.getWritableDatabase().update(
                        table.getName(),
                        values,
                        Column.TC_ID.getName() + " = ?",
                        new String[]{id}
                );
            }
        }
    }

    @NotNull
    private Cursor getProjectsOrBuildConfigurations(@NotNull String mainTable,
                                                    @NotNull String favouriteTable,
                                                    @NotNull String statusTable,
                                                    @NotNull String parentProjectId,
                                                    boolean onlyFavourite) {
        String idColumn = Column.TC_ID.getName();

        if (onlyFavourite) {
            return myDBHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM " + mainTable +
                            " INNER JOIN " + favouriteTable +
                            " ON " + mainTable + "." + idColumn + " = " + favouriteTable + "." + idColumn +
                            " AND " + favouriteTable + "." + Column.FAVOURITE.getName() + " = true" +
                            " LEFT JOIN " + statusTable +
                            " ON " + mainTable + "." + idColumn + " = " + statusTable + "." + idColumn +
                            " WHERE " + mainTable + "." + Column.PARENT_ID.getName() + " = " + parentProjectId,
                    new String[]{}
            );
        } else {
            return myDBHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM " + mainTable +
                            " LEFT JOIN " + favouriteTable +
                            " ON " + mainTable + "." + idColumn + " = " + favouriteTable + "." + idColumn +
                            " LEFT JOIN " + statusTable +
                            " ON " + mainTable + "." + idColumn + " = " + statusTable + "." + idColumn +
                            " WHERE " + mainTable + "." + Column.PARENT_ID.getName() + " = " + parentProjectId,
                    new String[]{}
            );
        }
    }

    private void setProjectOrBuildConfigurationStatus(@NotNull StatusTable table,
                                                      @NotNull String id,
                                                      @Nullable Status status) {
        if (status == null || status.equals(Status.DEFAULT)) {
            myDBHelper.getWritableDatabase().delete(
                    table.getName(),
                    Column.TC_ID.getName() + " = ?",
                    new String[]{id}
            );
        } else {
            Cursor cursor = myDBHelper.getReadableDatabase().query(
                    table.getName(),
                    null,
                    Column.TC_ID.getName() + " = ?",
                    new String[]{id},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), id);
                values.put(Column.STATUS.getName(), status.toString());

                myDBHelper.getWritableDatabase().insert(table.getName(), null, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(Column.STATUS.getName(), status.toString());

                myDBHelper.getWritableDatabase().update(
                        table.getName(),
                        values,
                        Column.TC_ID + " = ?",
                        new String[]{id}
                );
            }

            cursor.close();
        }
    }

    private void notifyListeners(@NotNull OverviewTable table) {
        for (Listener listener : myListeners.get(table)) {
            listener.onChanged();
        }
    }

    public static interface Listener {

        public void onChanged();
    }
}
