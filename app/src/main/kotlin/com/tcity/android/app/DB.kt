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

public class DB protected (context: Context) {

    private val dbHelper = DBHelper(context)

    throws(javaClass<SQLiteException>())
    public fun update(
            schema: Schema,
            values: ContentValues,
            whereClause: String,
            whereArgs: Array<String>
    ): Int {
        return dbHelper.getWritableDatabase().update(
                schema.tableName,
                values,
                whereClause,
                whereArgs
        )
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
    public fun delete(schema: Schema, whereClause: String? = null, whereArgs: Array<String>? = null): Int {
        return dbHelper.getWritableDatabase().delete(schema.tableName, whereClause, whereArgs)
    }

    throws(javaClass<SQLiteException>())
    public fun insert(schema: Schema, values: ContentValues): Long {
        return dbHelper.getWritableDatabase().insert(schema.tableName, null, values)
    }

    throws(javaClass<SQLiteException>())
    public fun beginTransaction(): Unit = dbHelper.getWritableDatabase().beginTransaction()

    throws(javaClass<SQLiteException>())
    public fun endTransaction(): Unit = dbHelper.getWritableDatabase().endTransaction()

    throws(javaClass<SQLiteException>(), javaClass<IllegalStateException>())
    public fun setTransactionSuccessful(): Unit = dbHelper.getWritableDatabase().setTransactionSuccessful()
}
