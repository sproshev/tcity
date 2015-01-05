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
                return column.getName() + " INTEGER NOT NULL";
            case ANDROID_ID:
                return column.getName() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
            default:
                throw new IllegalArgumentException();
        }
    }

    private void onFavouriteCreate(@NotNull FavouriteTable table,
                                   @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table.getName() + " (" +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.FAVOURITE) +
                        ");"
        );
    }

    private void onBuildOverviewCreate(@NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + OverviewTable.BUILD.getName() + " (" +
                        getDescription(Column.ANDROID_ID) + ", " +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.NAME) + ", " +
                        getDescription(Column.PARENT_ID) + ", " +
                        getDescription(Column.STATUS) +
                        ");"
        );
    }

    private void onProjectOrBuildConfigurationOverviewCreate(@NotNull OverviewTable table,
                                                             @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table.getName() + " (" +
                        getDescription(Column.ANDROID_ID) + ", " +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.NAME) + ", " +
                        getDescription(Column.PARENT_ID) +
                        ");"
        );
    }

    private void onProjectOrBuildConfigurationStatusesCreate(@NotNull StatusTable table,
                                                             @NotNull SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + table.getName() + " (" +
                        getDescription(Column.TC_ID) + ", " +
                        getDescription(Column.STATUS) +
                        ");"
        );
    }

}
