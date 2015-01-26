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
    private final Map<String, Collection<Listener>> myListeners;

    public DB(@NotNull Context context) {
        myDBHelper = new DBHelper(context);
        myListeners = new HashMap<>();

        myListeners.put(Constants.PROJECT_OVERVIEW_TABLE, new LinkedList<Listener>());
        myListeners.put(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE, new LinkedList<Listener>());
        myListeners.put(Constants.BUILD_OVERVIEW_TABLE, new LinkedList<Listener>());
    }

    // PROJECT - BEGIN

    public void setFavouriteProject(@NotNull String id, boolean favourite) {
        setFavourite(Constants.FAVOURITE_PROJECT_TABLE, id, favourite);

        notifyListeners(Constants.PROJECT_OVERVIEW_TABLE);
    }

    public void addFavouriteProjects(@NotNull Collection<String> ids) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            for (String id : ids) {
                Cursor cursor = db.query(
                        Constants.FAVOURITE_PROJECT_TABLE,
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

                    db.insert(Constants.FAVOURITE_PROJECT_TABLE, null, values);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Column.FAVOURITE.getName(), true);

                    db.update(
                            Constants.FAVOURITE_PROJECT_TABLE,
                            values,
                            Column.TC_ID.getName() + " = ?",
                            new String[]{id}
                    );
                }

                cursor.close();
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        notifyListeners(Constants.PROJECT_OVERVIEW_TABLE);
    }

    public boolean isProjectFavourite(@NotNull String id) {
        return isFavourite(Constants.FAVOURITE_PROJECT_TABLE, id);
    }

    @NotNull
    public String getProjectName(@NotNull String id) {
        return getName(Constants.PROJECT_OVERVIEW_TABLE, id);
    }

    public void setProjects(@NotNull Collection<Project> projects) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(Constants.PROJECT_OVERVIEW_TABLE, null, null);

            for (Project project : projects) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), project.id);
                values.put(Column.PARENT_ID.getName(), project.parentProjectId);
                values.put(Column.NAME.getName(), project.name);

                db.insert(Constants.PROJECT_OVERVIEW_TABLE, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        notifyListeners(Constants.PROJECT_OVERVIEW_TABLE);
    }

    @NotNull
    public Cursor getProjects(@Nullable String parentProjectId, boolean onlyFavourite) {
        return getProjectsOrBuildConfigurations(
                Constants.PROJECT_OVERVIEW_TABLE,
                Constants.FAVOURITE_PROJECT_TABLE,
                Constants.PROJECT_STATUS_TABLE,
                parentProjectId,
                onlyFavourite
        );
    }

    public void setProjectStatus(@NotNull String id, @Nullable Status status) {
        setProjectOrBuildConfigurationStatus(Constants.PROJECT_STATUS_TABLE, id, status);

        notifyListeners(Constants.PROJECT_OVERVIEW_TABLE);
    }

    // PROJECT - END

    // BUILD CONFIGURATION - BEGIN

    public void setFavouriteBuildConfiguration(@NotNull String id, boolean favourite) {
        setFavourite(Constants.FAVOURITE_BUILD_CONFIGURATION_TABLE, id, favourite);

        if (!favourite) {
            myDBHelper.getWritableDatabase().delete(
                    Constants.BUILD_CONFIGURATION_SYNC_BOUND_TABLE,
                    Column.TC_ID.getName() + " = ?",
                    new String[]{id}
            );
        }

        notifyListeners(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE);
    }

    public boolean isBuildConfigurationFavourite(@NotNull String id) {
        return isFavourite(Constants.FAVOURITE_BUILD_CONFIGURATION_TABLE, id);
    }

    @NotNull
    public String getBuildConfigurationName(@NotNull String id) {
        return getName(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE, id);
    }

    public void setBuildConfigurations(@NotNull String parentProjectId,
                                       @NotNull Collection<BuildConfiguration> buildConfigurations) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE,
                    Column.PARENT_ID.getName() + " = ?",
                    new String[]{parentProjectId}
            );

            for (BuildConfiguration buildConfiguration : buildConfigurations) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), buildConfiguration.id);
                values.put(Column.PARENT_ID.getName(), buildConfiguration.parentProjectId);
                values.put(Column.NAME.getName(), buildConfiguration.name);

                db.insert(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        notifyListeners(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE);
    }

    @NotNull
    public Cursor getBuildConfigurations(@Nullable String parentProjectId, boolean onlyFavourite) {
        return getProjectsOrBuildConfigurations(
                Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE,
                Constants.FAVOURITE_BUILD_CONFIGURATION_TABLE,
                Constants.BUILD_CONFIGURATION_STATUS_TABLE,
                parentProjectId,
                onlyFavourite
        );
    }

    public void setBuildConfigurationStatus(@NotNull String id, @Nullable Status status) {
        setProjectOrBuildConfigurationStatus(Constants.BUILD_CONFIGURATION_STATUS_TABLE, id, status);

        notifyListeners(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE);
    }

    public void setBuildConfigurationLastUpdate(@NotNull String id, long time) {
        setTime(id, time, Constants.BUILD_CONFIGURATION_LAST_UPDATE_TABLE);
    }

    public long getBuildConfigurationLastUpdate(@NotNull String id) {
        return getTime(Constants.BUILD_CONFIGURATION_LAST_UPDATE_TABLE, id);
    }

    public void setBuildConfigurationSyncBound(@NotNull String id, long time) {
        setTime(id, time, Constants.BUILD_CONFIGURATION_SYNC_BOUND_TABLE);
    }

    public long getBuildConfigurationSyncBound(@NotNull String id) {
        return getTime(Constants.BUILD_CONFIGURATION_SYNC_BOUND_TABLE, id);
    }

    // BUILD CONFIGURATION - END

    // BUILD - BEGIN

    public void setFavouriteBuild(@NotNull String id, boolean favourite) {
        setFavourite(Constants.FAVOURITE_BUILD_TABLE, id, favourite);

        notifyListeners(Constants.BUILD_OVERVIEW_TABLE);
    }

    public boolean isBuildFavourite(@NotNull String id) {
        return isFavourite(Constants.FAVOURITE_BUILD_TABLE, id);
    }

    public void setBuilds(@NotNull String parentBuildConfigurationId,
                          @NotNull Collection<Build> builds) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    Constants.BUILD_OVERVIEW_TABLE,
                    Column.PARENT_ID.getName() + " = ?",
                    new String[]{parentBuildConfigurationId}
            );

            for (Build build : builds) {
                ContentValues values = new ContentValues();
                values.put(Column.TC_ID.getName(), build.id);
                values.put(Column.PARENT_ID.getName(), build.parentBuildConfigurationId);
                values.put(Column.NAME.getName(), build.name);
                values.put(Column.STATUS.getName(), build.status.toString());
                values.put(Column.BRANCH.getName(), build.branch);

                db.insert(Constants.BUILD_OVERVIEW_TABLE, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        notifyListeners(Constants.BUILD_OVERVIEW_TABLE);
    }

    public void appendBuilds(@NotNull Collection<Build> builds) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            for (Build build : builds) {
                appendBuild(build);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        notifyListeners(Constants.BUILD_OVERVIEW_TABLE);
    }

    @NotNull
    public Cursor getBuilds(@NotNull String parentBuildConfigurationId, boolean onlyFavourite) {
        String mainTable = Constants.BUILD_OVERVIEW_TABLE;
        String favouriteTable = Constants.FAVOURITE_BUILD_TABLE;

        String idColumn = Column.TC_ID.getName();

        if (onlyFavourite) {
            return myDBHelper.getReadableDatabase().rawQuery(
                    "SELECT " +
                            mainTable + "." + Column.ANDROID_ID.getName() + ", " +
                            mainTable + "." + Column.TC_ID.getName() + ", " +
                            mainTable + "." + Column.NAME.getName() + ", " +
                            mainTable + "." + Column.PARENT_ID.getName() + ", " +
                            mainTable + "." + Column.STATUS.getName() + ", " +
                            mainTable + "." + Column.BRANCH.getName() + ", " +
                            favouriteTable + "." + Column.FAVOURITE.getName() +
                            " FROM " + mainTable +
                            " INNER JOIN " + favouriteTable +
                            " ON " + mainTable + "." + idColumn + " = " + favouriteTable + "." + idColumn +
                            " AND " + favouriteTable + "." + Column.FAVOURITE.getName() +
                            " WHERE " + mainTable + "." + Column.PARENT_ID.getName() + " = '" + parentBuildConfigurationId + "'",
                    null
            );
        } else {
            return myDBHelper.getReadableDatabase().rawQuery(
                    "SELECT " +
                            mainTable + "." + Column.ANDROID_ID.getName() + ", " +
                            mainTable + "." + Column.TC_ID.getName() + ", " +
                            mainTable + "." + Column.NAME.getName() + ", " +
                            mainTable + "." + Column.PARENT_ID.getName() + ", " +
                            mainTable + "." + Column.STATUS.getName() + ", " +
                            mainTable + "." + Column.BRANCH.getName() + ", " +
                            favouriteTable + "." + Column.FAVOURITE.getName() +
                            " FROM " + mainTable +
                            " LEFT JOIN " + favouriteTable +
                            " ON " + mainTable + "." + idColumn + " = " + favouriteTable + "." + idColumn +
                            " WHERE " + mainTable + "." + Column.PARENT_ID.getName() + " = '" + parentBuildConfigurationId + "'",
                    null
            );
        }
    }

    // BUILD - END

    public synchronized void addProjectsListener(@NotNull Listener listener) {
        myListeners.get(Constants.PROJECT_OVERVIEW_TABLE).add(listener);
    }

    public synchronized void addBuildConfigurationsListener(@NotNull Listener listener) {
        myListeners.get(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE).add(listener);
    }

    public synchronized void addBuildsListener(@NotNull Listener listener) {
        myListeners.get(Constants.BUILD_OVERVIEW_TABLE).add(listener);
    }

    public synchronized void removeProjectsListener(@NotNull Listener listener) {
        myListeners.get(Constants.PROJECT_OVERVIEW_TABLE).remove(listener);
    }

    public synchronized void removeBuildConfigurationsListener(@NotNull Listener listener) {
        myListeners.get(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE).remove(listener);
    }

    public synchronized void removeBuildsListener(@NotNull Listener listener) {
        myListeners.get(Constants.BUILD_OVERVIEW_TABLE).remove(listener);
    }

    public void reset() {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();

        db.delete(Constants.FAVOURITE_PROJECT_TABLE, null, null);
        db.delete(Constants.FAVOURITE_BUILD_CONFIGURATION_TABLE, null, null);
        db.delete(Constants.FAVOURITE_BUILD_TABLE, null, null);

        db.delete(Constants.PROJECT_OVERVIEW_TABLE, null, null);
        db.delete(Constants.BUILD_CONFIGURATION_OVERVIEW_TABLE, null, null);
        db.delete(Constants.BUILD_OVERVIEW_TABLE, null, null);

        db.delete(Constants.PROJECT_STATUS_TABLE, null, null);
        db.delete(Constants.BUILD_CONFIGURATION_STATUS_TABLE, null, null);

        db.delete(Constants.BUILD_CONFIGURATION_LAST_UPDATE_TABLE, null, null);
        db.delete(Constants.BUILD_CONFIGURATION_SYNC_BOUND_TABLE, null, null);
    }

    private void setFavourite(@NotNull String table,
                              @NotNull String id,
                              boolean favourite) {
        if (!favourite) {
            myDBHelper.getWritableDatabase().delete(
                    table,
                    Column.TC_ID + " = ?",
                    new String[]{id}
            );
        } else {
            Cursor cursor = myDBHelper.getReadableDatabase().query(
                    table,
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

                myDBHelper.getWritableDatabase().insert(table, null, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(Column.FAVOURITE.getName(), true);

                myDBHelper.getWritableDatabase().update(
                        table,
                        values,
                        Column.TC_ID.getName() + " = ?",
                        new String[]{id}
                );
            }
        }
    }

    private boolean isFavourite(@NotNull String table, @NotNull String id) {
        Cursor cursor = myDBHelper.getReadableDatabase().query(
                table,
                null,
                Column.TC_ID.getName() + " = ?",
                new String[]{id},
                null,
                null,
                null
        );

        if (cursor.getCount() == 0) {
            cursor.close();

            return false;
        } else {
            cursor.moveToNext();

            boolean result = DBUtils.getFavourite(cursor);

            cursor.close();

            return result;
        }
    }

    @NotNull
    private String getName(@NotNull String table, @NotNull String id) {
        Cursor cursor = myDBHelper.getReadableDatabase().query(
                table,
                new String[]{Column.NAME.getName()},
                Column.TC_ID.getName() + " = ?",
                new String[]{id},
                null, null, null
        );

        cursor.moveToNext();

        String result = DBUtils.getName(cursor);

        cursor.close();

        return result;
    }

    @NotNull
    private Cursor getProjectsOrBuildConfigurations(@NotNull String mainTable,
                                                    @NotNull String favouriteTable,
                                                    @NotNull String statusTable,
                                                    @Nullable String parentProjectId,
                                                    boolean onlyFavourite) {
        String idColumn = Column.TC_ID.getName();
        String query = "SELECT " +
                mainTable + "." + Column.ANDROID_ID.getName() + ", " +
                mainTable + "." + Column.TC_ID.getName() + ", " +
                mainTable + "." + Column.NAME.getName() + ", " +
                mainTable + "." + Column.PARENT_ID.getName() + ", " +
                favouriteTable + "." + Column.FAVOURITE.getName() + ", " +
                statusTable + "." + Column.STATUS.getName() +
                " FROM " + mainTable + " " +
                (
                        onlyFavourite
                                ?
                                "INNER"
                                :
                                "LEFT"
                ) + " JOIN " + favouriteTable +
                " ON " + mainTable + "." + idColumn + " = " + favouriteTable + "." + idColumn +
                (
                        onlyFavourite
                                ?
                                " AND " + favouriteTable + "." + Column.FAVOURITE.getName()
                                :
                                ""
                ) +
                " LEFT JOIN " + statusTable +
                " ON " + mainTable + "." + idColumn + " = " + statusTable + "." + idColumn +
                (
                        parentProjectId != null
                                ?
                                " WHERE " + mainTable + "." + Column.PARENT_ID.getName() + " = '" + parentProjectId + "'"
                                :
                                ""
                );

        return myDBHelper.getReadableDatabase().rawQuery(query, null);
    }

    private void setProjectOrBuildConfigurationStatus(@NotNull String table,
                                                      @NotNull String id,
                                                      @Nullable Status status) {
        if (status == null || status.equals(Status.DEFAULT)) {
            myDBHelper.getWritableDatabase().delete(
                    table,
                    Column.TC_ID.getName() + " = ?",
                    new String[]{id}
            );
        } else {
            Cursor cursor = myDBHelper.getReadableDatabase().query(
                    table,
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

                myDBHelper.getWritableDatabase().insert(table, null, values);
            } else {
                ContentValues values = new ContentValues();
                values.put(Column.STATUS.getName(), status.toString());

                myDBHelper.getWritableDatabase().update(
                        table,
                        values,
                        Column.TC_ID + " = ?",
                        new String[]{id}
                );
            }

            cursor.close();
        }
    }

    private void setTime(@NotNull String id, long time, @NotNull String table) {
        Cursor cursor = myDBHelper.getReadableDatabase().query(
                table,
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
            values.put(Column.TIME.getName(), time);

            myDBHelper.getWritableDatabase().insert(table, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(Column.TIME.getName(), time);

            myDBHelper.getWritableDatabase().update(
                    table,
                    values,
                    Column.TC_ID.getName() + " = ?",
                    new String[]{id}
            );
        }

        cursor.close();
    }

    private long getTime(@NotNull String table, @NotNull String id) {
        Cursor cursor = myDBHelper.getReadableDatabase().query(
                table,
                null,
                Column.TC_ID.getName() + " = ?",
                new String[]{id},
                null,
                null,
                null
        );

        if (cursor.getCount() == 0) {
            cursor.close();

            return DBUtils.UNDEFINED_TIME;
        } else {
            cursor.moveToNext();

            long result = cursor.getLong(cursor.getColumnIndex(Column.TIME.getName()));

            cursor.close();

            return result;
        }
    }

    private void appendBuild(@NotNull Build build) {
        Cursor cursor = myDBHelper.getReadableDatabase().query(
                Constants.BUILD_OVERVIEW_TABLE,
                null,
                Column.TC_ID.getName() + " = ?",
                new String[]{build.id},
                null, null, null
        );

        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Column.TC_ID.getName(), build.id);
            values.put(Column.PARENT_ID.getName(), build.parentBuildConfigurationId);
            values.put(Column.NAME.getName(), build.name);
            values.put(Column.STATUS.getName(), build.status.toString());
            values.put(Column.BRANCH.getName(), build.branch);

            myDBHelper.getWritableDatabase().insert(Constants.BUILD_OVERVIEW_TABLE, null, values);
        } else {
            ContentValues values = new ContentValues();
            values.put(Column.PARENT_ID.getName(), build.parentBuildConfigurationId);
            values.put(Column.NAME.getName(), build.name);
            values.put(Column.STATUS.getName(), build.status.toString());
            values.put(Column.BRANCH.getName(), build.branch);

            myDBHelper.getWritableDatabase().update(
                    Constants.BUILD_OVERVIEW_TABLE,
                    values,
                    Column.TC_ID + " = ?",
                    new String[]{build.id}
            );
        }

        cursor.close();
    }

    private void notifyListeners(@NotNull String table) {
        for (Listener listener : myListeners.get(table)) {
            listener.onChanged();
        }
    }

    public static interface Listener {

        public void onChanged();
    }
}
