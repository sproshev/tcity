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
import android.database.Cursor

// Custom - BEGIN

public val Concept.dbValues: ContentValues
    get() {
        val result = ContentValues()

        result.put(Schema.TC_ID_COLUMN, id)
        result.put(Schema.NAME_COLUMN, name)
        result.put(Schema.PARENT_ID_COLUMN, parentId)
        result.put(Schema.STATUS_COLUMN, status.toString())
        result.put(Schema.WATCHED_COLUMN, watched.dbValue)

        return result
    }

public val Status.dbValues: ContentValues
    get () {
        val result = ContentValues()

        result.put(Schema.STATUS_COLUMN, toString())

        return result
    }

public fun getStatus(cursor: Cursor): Status {
    return Status.valueOf(
            cursor.getString(
                    cursor.getColumnIndex(Schema.STATUS_COLUMN)
            )
    )
}

public fun getId(cursor: Cursor): String {
    return cursor.getString(
            cursor.getColumnIndex(Schema.TC_ID_COLUMN)
    )
}

public fun getName(cursor: Cursor): String {
    return cursor.getString(
            cursor.getColumnIndex(Schema.NAME_COLUMN)
    )
}

// Custom - END

// Primitive - BEGIN

public fun Boolean.dbValues(column: String): ContentValues {
    val result = ContentValues()

    result.put(column, dbValue)

    return result
}

public val Boolean.dbValue: Int
    get() = if (this) 1 else 0

public fun getBoolean(cursor: Cursor, column: String): Boolean {
    return cursor.getInt(cursor.getColumnIndex(column)) != 0
}

// Primitive - END
