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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectionUtils {

    private SelectionUtils() {
    }

    @Nullable
    public static String getSelection(@Nullable String value,
                                      @NotNull String column,
                                      @NotNull String... required) {
        if (required.length == 0) {
            if (value == null) {
                return null;
            } else {
                return column + " = ?";
            }
        } else {
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < required.length; i++) {
                result.append(required[i]).append(" = ?");

                if (i != required.length - 1) {
                    result.append(" AND ");
                }
            }

            if (value != null) {
                result.append(" AND ").append(column).append(" = ?");
            }

            return result.toString();
        }
    }

    @Nullable
    public static String[] getSelectionArgs(@Nullable String column, @NotNull String... required) {
        if (required.length == 0) {
            if (column == null) {
                return null;
            } else {
                return new String[]{column};
            }
        } else {
            if (column == null) {
                return required;
            } else {
                String[] result = new String[required.length + 1];

                System.arraycopy(required, 0, result, 0, required.length);

                result[required.length] = column;

                return result;
            }
        }
    }
}
