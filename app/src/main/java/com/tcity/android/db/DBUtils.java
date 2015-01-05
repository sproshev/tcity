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

import android.database.Cursor;

import com.tcity.android.Status;

import org.jetbrains.annotations.NotNull;

public class DBUtils {

    private DBUtils() {
    }

    @NotNull
    public static Status getStatus(@NotNull Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(Column.STATUS.getName());

        if (cursor.isNull(columnIndex)) {
            return Status.DEFAULT;
        } else {
            return Status.valueOf(cursor.getString(columnIndex));
        }
    }

    public static boolean getFavourite(@NotNull Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(Column.FAVOURITE.getName());

        if (cursor.isNull(columnIndex)) {
            return false;
        } else {
            return cursor.getInt(columnIndex) != 0;
        }
    }

    @NotNull
    public static String getId(@NotNull Cursor cursor) {
        return cursor.getString(
                cursor.getColumnIndex(
                        Column.TC_ID.getName()
                )
        );
    }

    @NotNull
    public static String getName(@NotNull Cursor cursor) {
        return cursor.getString(
                cursor.getColumnIndex(
                        Column.NAME.getName()
                )
        );
    }
}
