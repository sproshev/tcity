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

import com.tcity.android.Status;
import com.tcity.android.client.parser.Concept;

import org.jetbrains.annotations.NotNull;

public class CVUtils {

    private CVUtils() {
    }

    @NotNull
    public static ContentValues toContentValues(@NotNull Concept concept) {
        ContentValues result = new ContentValues();

        result.put(Schema.TC_ID_COLUMN, concept.getId());
        result.put(Schema.NAME_COLUMN, concept.getName());
        result.put(Schema.PARENT_ID_COLUMN, concept.getParentId());

        result.putAll(toContentValues(concept.getStatus()));
        result.putAll(toFavouriteContentValues(false));

        return result;
    }

    @NotNull
    public static ContentValues toContentValues(@NotNull Status status) {
        ContentValues result = new ContentValues();

        result.put(Schema.STATUS_COLUMN, status.toString());

        return result;
    }

    @NotNull
    public static ContentValues toFavouriteContentValues(boolean b) {
        ContentValues result = new ContentValues();

        result.put(Schema.FAVOURITE_COLUMN, toContentValue(b));

        return result;
    }

    @NotNull
    public static String toFavouriteContentValue(boolean b) {
        return Integer.toString(toContentValue(b));
    }

    private static int toContentValue(boolean b) {
        return b ? 1 : 0;
    }
}
