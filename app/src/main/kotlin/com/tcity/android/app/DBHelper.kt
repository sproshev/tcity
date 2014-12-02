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
import com.tcity.android.db.BuildSchema
import com.tcity.android.db.BuildConfigurationSchema
import com.tcity.android.db.ProjectSchema
import android.util.Log

private class DBHelper(context: Context) : SQLiteOpenHelper(context, "tcity", null, 1) {

    class object {
        private val LOG_TAG = javaClass<DBHelper>().getSimpleName()
        private val SCHEMAS = listOf(BuildSchema, BuildConfigurationSchema, ProjectSchema)
    }

    override fun onCreate(db: SQLiteDatabase) {
        create(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        upgrade(db, oldVersion, newVersion)
    }

    private fun create(db: SQLiteDatabase) {
        SCHEMAS.forEach {
            db.execSQL(it.createScript)

            Log.d(LOG_TAG, "Table was created [name: ${it.tableName}]")
        }
    }

    private fun upgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        drop(db)
        create(db)

        Log.d(
                LOG_TAG,
                "DB was upgraded: [oldVersion: $oldVersion, newVersion: $newVersion]"
        )
    }

    private fun drop(db: SQLiteDatabase) {
        SCHEMAS.forEach {
            db.execSQL(it.dropScript)

            Log.d(LOG_TAG, "Table was dropped [name: ${it.tableName}]")
        }
    }
}