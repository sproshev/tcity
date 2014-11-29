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

import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import android.util.Log

public abstract class DBHelper(
        private val schema: Schema,
        context: Context,
        name: String,
        version: Int
) : SQLiteOpenHelper(context, name, null, version) {

    class object {
        private val LOG_TAG = javaClass<DBHelper>().getSimpleName()
    }

    override fun onCreate(db: SQLiteDatabase) {
        create(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        upgrade(db, oldVersion, newVersion)
    }

    private fun create(db: SQLiteDatabase) {
        db.execSQL(schema.createScript)

        Log.d(LOG_TAG, "DB was created [schema: ${schema.tableName}]")
    }

    private fun upgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        drop(db)
        create(db)

        Log.d(
                LOG_TAG,
                "DB was upgraded: [schema: ${schema.tableName}, oldVersion: $oldVersion, newVersion: $newVersion]"
        )
    }

    private fun drop(db: SQLiteDatabase) {
        db.execSQL(schema.dropScript)

        Log.d(LOG_TAG, "DB was dropped [schema: ${schema.tableName}]")
    }
}

public class BuildDBHelper(context: Context) : DBHelper(BUILD_SCHEMA, context, "builds", 1)

public class BuildDBSchemaHelper(context: Context) : DBHelper(BUILD_CONFIGURATION_SCHEMA, context, "buildconfigurations", 1)

public class ProjectDBHelper(context: Context) : DBHelper(PROJECT_SCHEMA, context, "projects", 1)



