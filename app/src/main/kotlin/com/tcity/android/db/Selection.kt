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

package com.tcity.android.db

public fun calculateSelection(optional: String?, column: String, vararg required: String): String? {
    return if (required.isEmpty()) {
        if (optional == null) {
            null
        } else {
            "$column = ?"
        }
    } else {
        StringBuilder {
            required.stream().map { "$it = ?" }.joinTo(this, " AND ")

            if (optional != null) {
                append(" AND $column = ?")
            }
        }.toString()
    }
}

public fun calculateSelectionArgs(optional: String?, vararg required: String): Array<String>? {
    return if (required.isEmpty()) {
        if (optional == null) {
            null
        } else {
            array(optional)
        }
    } else {
        if (optional == null) {
            required
        } else {
            return Array(
                    1 + required.size,
                    {
                        if (it < required.size) required[it] else optional
                    }
            )
        }
    }
}


