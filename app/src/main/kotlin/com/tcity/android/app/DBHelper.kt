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
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import com.tcity.android.db.Schema

private class DBHelper(context: Context, private val schemas: Collection<Schema>) : SQLiteOpenHelper(context, null, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        create(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        drop(db)
        create(db)
    }

    private fun create(db: SQLiteDatabase) {
        schemas.forEach {
            db.execSQL(it.createScript)
        }
    }

    private fun drop(db: SQLiteDatabase) {
        schemas.forEach {
            db.execSQL(it.dropScript)
        }
    }
}