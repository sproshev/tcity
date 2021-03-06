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
        onFavouriteCreate(Table.FAVOURITE_PROJECT_TABLE, db);
        onFavouriteCreate(Table.FAVOURITE_BUILD_CONFIGURATION_TABLE, db);
        onFavouriteCreate(Table.FAVOURITE_BUILD_TABLE, db);

        onProjectOverviewCreate(Table.PROJECT_OVERVIEW_TABLE, db);
        onBuildConfigurationOverviewCreate(Table.BUILD_CONFIGURATION_OVERVIEW_TABLE, db);
        onBuildOverviewCreate(db);

        onProjectOrBuildConfigurationStatusCreate(Table.PROJECT_STATUS_TABLE, db);
        onProjectOrBuildConfigurationStatusCreate(Table.BUILD_CONFIGURATION_STATUS_TABLE, db);

        onProjectTimeCreate(db);
        onBuildConfigurationTimeCreate(db);
    }

    @Override
    public void onUpgrade(@NotNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Table.FAVOURITE_PROJECT_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Table.FAVOURITE_BUILD_CONFIGURATION_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Table.FAVOURITE_BUILD_TABLE + ";");

        db.execSQL("DROP TABLE IF EXISTS " + Table.PROJECT_OVERVIEW_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Table.BUILD_CONFIGURATION_OVERVIEW_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Table.BUILD_OVERVIEW_TABLE + ";");

        db.execSQL("DROP TABLE IF EXISTS " + Table.PROJECT_STATUS_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Table.BUILD_CONFIGURATION_STATUS_TABLE + ";");

        db.execSQL("DROP TABLE IF EXISTS " + Table.PROJECT_TIME_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Table.BUILD_CONFIGURATION_TIME_TABLE + ";");

        onCreate(db);
    }

    @NotNull
    private String getDescription(@NotNull Column column) {
        switch (column) {
            case TC_ID:
                return column.getName() + " TEXT NOT NULL UNIQUE";
            case NAME:
            case PARENT_ID:
            case STATUS:
                return column.getName() + " TEXT NOT NULL";
            case FAVOURITE:
            case BRANCH_DEFAULT:
            case PAUSED:
            case ARCHIVED:
                return column.getName() + " INTEGER NOT NULL";
            case LAST_UPDATE:
            case SYNC_BOUND:
            case STATUS_UPDATE:
                return column.getName() + " INTEGER";
            case ANDROID_ID:
                return column.getName() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
            case BRANCH:
                return column.getName() + " TEXT";
            default:
                throw new IllegalArgumentException();
        }
    }

    private void onFavouriteCreate(@NotNull String table,
                                   @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table + " (" +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.FAVOURITE) +
                        ");"
        );
    }

    private void onBuildOverviewCreate(@NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + Table.BUILD_OVERVIEW_TABLE + " (" +
                        getDescription(Column.ANDROID_ID) + ", " +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.NAME) + ", " +
                        getDescription(Column.PARENT_ID) + ", " +
                        getDescription(Column.STATUS) + ", " +
                        getDescription(Column.BRANCH) + ", " +
                        getDescription(Column.BRANCH_DEFAULT) +
                        ");"
        );
    }

    private void onProjectOverviewCreate(@NotNull String table,
                                         @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table + " (" +
                        getDescription(Column.ANDROID_ID) + ", " +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.NAME) + ", " +
                        getDescription(Column.PARENT_ID) + ", " +
                        getDescription(Column.ARCHIVED) +
                        ");"
        );
    }

    private void onBuildConfigurationOverviewCreate(@NotNull String table,
                                                    @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table + " (" +
                        getDescription(Column.ANDROID_ID) + ", " +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.NAME) + ", " +
                        getDescription(Column.PARENT_ID) + ", " +
                        getDescription(Column.PAUSED) +
                        ");"
        );
    }

    private void onProjectOrBuildConfigurationStatusCreate(@NotNull String table,
                                                           @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table + " (" +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.STATUS) +
                        ");"
        );
    }

    private void onProjectTimeCreate(@NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + Table.PROJECT_TIME_TABLE + " (" +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.LAST_UPDATE) + ", " +
                        getDescription(Column.STATUS_UPDATE) +
                        ");"
        );
    }

    private void onBuildConfigurationTimeCreate(@NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + Table.BUILD_CONFIGURATION_TIME_TABLE + " (" +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.LAST_UPDATE) + ", " +
                        getDescription(Column.SYNC_BOUND) + ", " +
                        getDescription(Column.STATUS_UPDATE) +
                        ");"
        );
    }
}
