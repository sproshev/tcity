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

public val TC_ID_COLUMN: String = "tc_id"
public val NAME_COLUMN: String = "name"
public val PARENT_ID_COLUMN: String = "parent_id"
public val STATUS_COLUMN: String = "status"
public val WATCHED_COLUMN: String = "watched"
public val ANDROID_ID_COLUMN: String = "_id"

public enum class Schema {

    BUILD {
        override val tableName = "Build"
        override val columnTypes = mapOf(
                ANDROID_ID_COLUMN.to("INTEGER NOT NULL UNIQUE"),
                TC_ID_COLUMN.to("TEXT NOT NULL UNIQUE"),
                NAME_COLUMN.to("TEXT NOT NULL"),
                PARENT_ID_COLUMN.to("TEXT NOT NULL"),
                STATUS_COLUMN.to("TEXT NOT NULL"),
                WATCHED_COLUMN.to("INTEGER NOT NULL")
        )
    }

    BUILD_CONFIGURATION {
        override val tableName = "BuildConfiguration"
        override val columnTypes = mapOf(
                ANDROID_ID_COLUMN.to("INTEGER NOT NULL UNIQUE"),
                TC_ID_COLUMN.to("TEXT NOT NULL UNIQUE"),
                NAME_COLUMN.to("TEXT NOT NULL"),
                PARENT_ID_COLUMN.to("TEXT NOT NULL"),
                STATUS_COLUMN.to("TEXT NOT NULL"),
                WATCHED_COLUMN.to("INTEGER NOT NULL")
        )
    }

    PROJECT {
        override val tableName = "Project"
        override val columnTypes = mapOf(
                ANDROID_ID_COLUMN.to("INTEGER NOT NULL UNIQUE"),
                TC_ID_COLUMN.to("TEXT NOT NULL UNIQUE"),
                NAME_COLUMN.to("TEXT NOT NULL"),
                PARENT_ID_COLUMN.to("TEXT NOT NULL"),
                STATUS_COLUMN.to("TEXT NOT NULL"),
                WATCHED_COLUMN.to("INTEGER NOT NULL")
        )
    }

    public abstract val tableName: String
    public abstract val columnTypes: Map<String, String>
}


