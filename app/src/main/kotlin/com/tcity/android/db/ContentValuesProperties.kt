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

import android.content.ContentValues
import com.tcity.android.concept.Concept
import com.tcity.android.concept.Status

public val Concept.contentValues: ContentValues
    get() {
        val result = ContentValues()

        result.put(TC_ID_COLUMN, id)
        result.put(NAME_COLUMN, name)
        result.put(PARENT_ID_COLUMN, parentId)
        result.put(STATUS_COLUMN, status.contentValue)
        result.put(WATCHED_COLUMN, watched.contentValue)

        return result
    }

public val Status.contentValues: ContentValues
    get () {
        val result = ContentValues()

        result.put(STATUS_COLUMN, contentValue)

        return result
    }

public fun Boolean.contentValues(column: String): ContentValues {
    val result = ContentValues()

    result.put(column, contentValue)

    return result
}

private val Status.contentValue: String
    get() = toString()

private val Boolean.contentValue: Int
    get() = if (this) 1 else 0
