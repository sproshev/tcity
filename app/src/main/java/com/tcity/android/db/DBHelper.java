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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

class DBHelper extends SQLiteOpenHelper {

    DBHelper(@NotNull Context context) {
        super(context, "tcity", null, 1);
    }

    @Override
    public void onCreate(@NotNull SQLiteDatabase db) {
        onFavouriteCreate(FavouriteTable.PROJECT, db);
        onFavouriteCreate(FavouriteTable.BUILD_CONFIGURATION, db);
        onFavouriteCreate(FavouriteTable.BUILD, db);

        onProjectOrBuildConfigurationOverviewCreate(OverviewTable.PROJECT, db);
        onProjectOrBuildConfigurationOverviewCreate(OverviewTable.BUILD_CONFIGURATION, db);
        onBuildOverviewCreate(db);

        onProjectOrBuildConfigurationStatusesCreate(StatusTable.PROJECT, db);
        onProjectOrBuildConfigurationStatusesCreate(StatusTable.BUILD_CONFIGURATION, db);
    }

    @Override
    public void onUpgrade(@NotNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteTable.PROJECT.getName() + ";");
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteTable.BUILD_CONFIGURATION.getName() + ";");
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteTable.BUILD.getName() + ";");

        db.execSQL("DROP TABLE IF EXISTS " + OverviewTable.PROJECT.getName() + ";");
        db.execSQL("DROP TABLE IF EXISTS " + OverviewTable.BUILD_CONFIGURATION.getName() + ";");
        db.execSQL("DROP TABLE IF EXISTS " + OverviewTable.BUILD.getName() + ";");

        db.execSQL("DROP TABLE IF EXISTS " + StatusTable.PROJECT.getName() + ";");
        db.execSQL("DROP TABLE IF EXISTS " + StatusTable.BUILD_CONFIGURATION.getName() + ";");

        onCreate(db);
    }

    private void onFavouriteCreate(@NotNull FavouriteTable table,
                                   @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table.getName() + " (" +
                        Column.TC_ID.getDescription() + ", " +
                        Column.FAVOURITE.getDescription() +
                        ");"
        );
    }

    private void onBuildOverviewCreate(@NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + OverviewTable.BUILD.getName() + " (" +
                        Column.ANDROID_ID.getDescription() + ", " +
                        Column.TC_ID.getDescription() + ", " +
                        Column.NAME.getDescription() + ", " +
                        Column.PARENT_ID.getDescription() + ", " +
                        Column.STATUS.getDescription() +
                        ");"
        );
    }

    private void onProjectOrBuildConfigurationOverviewCreate(@NotNull OverviewTable table,
                                                             @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table.getName() + " (" +
                        Column.ANDROID_ID.getDescription() + ", " +
                        Column.TC_ID.getDescription() + ", " +
                        Column.NAME.getDescription() + ", " +
                        Column.PARENT_ID.getDescription() +
                        ");"
        );
    }

    private void onProjectOrBuildConfigurationStatusesCreate(@NotNull StatusTable table,
                                                             @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table.getName() + " (" +
                        Column.TC_ID.getDescription() + ", " +
                        Column.STATUS.getDescription() +
                        ");"
        );
    }

    static enum FavouriteTable {
        PROJECT, BUILD_CONFIGURATION, BUILD;

        @NotNull
        String getName() {
            switch (this) {
                case PROJECT:
                    return "favourite_project";
                case BUILD_CONFIGURATION:
                    return "favourite_build_configuration";
                case BUILD:
                    return "favourite_build";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    static enum OverviewTable {
        PROJECT, BUILD_CONFIGURATION, BUILD;

        @NotNull
        String getName() {
            switch (this) {
                case PROJECT:
                    return "project_overview";
                case BUILD_CONFIGURATION:
                    return "build_configuration_overview";
                case BUILD:
                    return "build_overview";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    static enum StatusTable {
        PROJECT, BUILD_CONFIGURATION;

        @NotNull
        String getName() {
            switch (this) {
                case PROJECT:
                    return "project_status";
                case BUILD_CONFIGURATION:
                    return "build_configuration_status";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    static enum Column {
        TC_ID, NAME, PARENT_ID, STATUS, FAVOURITE, ANDROID_ID;

        @NotNull
        String getName() {
            switch (this) {
                case TC_ID:
                    return "tc_id";
                case NAME:
                    return "name";
                case PARENT_ID:
                    return "parent_id";
                case STATUS:
                    return "status";
                case FAVOURITE:
                    return "favourite";
                case ANDROID_ID:
                    return "_id";
                default:
                    throw new IllegalArgumentException();
            }
        }

        @NotNull
        private String getDescription() {
            switch (this) {
                case TC_ID:
                    return getName() + " TEXT NOT NULL UNIQUE";
                case NAME:
                case PARENT_ID:
                case STATUS:
                    return getName() + " TEXT NOT NULL";
                case FAVOURITE:
                    return getName() + " INTEGER NOT NULL";
                case ANDROID_ID:
                    return getName() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
