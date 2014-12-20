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

package com.tcity.android.app

import android.content.Context
import com.tcity.android.db.Schema
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import java.util.HashMap
import java.util.LinkedList
import com.tcity.android.db.SelectionUtils

public class DB protected (context: Context) {

    private val dbHelper = DBHelper(context)

    private val listeners: MutableMap<Schema, MutableList<Schema.Listener>>

    {
        listeners = HashMap()

        Schema.values().forEach {
            listeners.put(it, LinkedList())
        }
    }

    synchronized
    public fun addListener(schema: Schema, listener: Schema.Listener) {
        listeners.get(schema)!!.add(listener)
    }

    synchronized
    public fun removeListener(schema: Schema, listener: Schema.Listener) {
        listeners.get(schema)!!.remove(listener)
    }

    // SQL - BEGIN

    throws(javaClass<SQLiteException>())
    public fun update(
            schema: Schema,
            values: ContentValues,
            whereClause: String? = null,
            whereArgs: Array<String>? = null
    ): Int {
        val result = dbHelper.getWritableDatabase().update(
                schema.tableName,
                values,
                whereClause,
                whereArgs
        )

        notifyListeners(schema)

        return result
    }

    throws(javaClass<SQLiteException>())
    public fun query(
            schema: Schema,
            columns: Array<String>? = null,
            selection: String? = null,
            selectionArgs: Array<String>? = null,
            groupBy: String? = null,
            having: String? = null,
            orderBy: String? = null,
            limit: String? = null
    ): Cursor {
        return dbHelper.getReadableDatabase().query(
                schema.tableName,
                columns,
                selection,
                selectionArgs,
                groupBy,
                having,
                orderBy,
                limit
        )
    }

    throws(javaClass<SQLiteException>())
    public fun set(
            schema: Schema,
            values: Collection<ContentValues>,
            parentId: String?
    ): Int {
        val db = dbHelper.getWritableDatabase()
        var result = 0

        db.beginTransaction()

        try {
            db.delete(
                    schema.tableName,
                    SelectionUtils.getSelection(parentId, Schema.PARENT_ID_COLUMN),
                    SelectionUtils.getSelectionArgs(parentId)
            )

            values.forEach {
                db.insert(schema.tableName, null, it)
                result++
            }

            db.setTransactionSuccessful()

            notifyListeners(schema)
        } finally {
            db.endTransaction()

            result = 0
        }

        return result
    }

    // SQL - END

    private fun notifyListeners(schema: Schema) {
        listeners.get(schema)!!.forEach {
            it.onChanged()
        }
    }
}
