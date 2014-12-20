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

public enum class Schema {

    class object {
        public val TC_ID_COLUMN: String = "tc_id"
        public val NAME_COLUMN: String = "name"
        public val PARENT_ID_COLUMN: String = "parent_id"
        public val STATUS_COLUMN: String = "status"
        public val FAVOURITE_COLUMN: String = "favourite"

        private val TC_ID_COLUMN_DESC = TC_ID_COLUMN.to("TEXT NOT NULL UNIQUE")
        private val NAME_COLUMN_DESC = NAME_COLUMN.to("TEXT NOT NULL")
        private val PARENT_ID_COLUMN_DESC = PARENT_ID_COLUMN.to("TEXT NOT NULL")
        private val STATUS_COLUMN_DESC = STATUS_COLUMN.to("TEXT NOT NULL")
        private val FAVOURITE_COLUMN_DESC = FAVOURITE_COLUMN.to("INTEGER NOT NULL")
    }

    BUILD {
        override val tableName = "Build"

        override val columnTypes = mapOf(
                TC_ID_COLUMN_DESC,
                NAME_COLUMN_DESC,
                PARENT_ID_COLUMN_DESC,
                STATUS_COLUMN_DESC,
                FAVOURITE_COLUMN_DESC
        )
    }

    BUILD_CONFIGURATION {
        override val tableName = "BuildConfiguration"

        override val columnTypes = mapOf(
                TC_ID_COLUMN_DESC,
                NAME_COLUMN_DESC,
                PARENT_ID_COLUMN_DESC,
                STATUS_COLUMN_DESC,
                FAVOURITE_COLUMN_DESC
        )
    }

    PROJECT {
        override val tableName = "Project"

        override val columnTypes = mapOf(
                TC_ID_COLUMN_DESC,
                NAME_COLUMN_DESC,
                PARENT_ID_COLUMN_DESC,
                STATUS_COLUMN_DESC,
                FAVOURITE_COLUMN_DESC
        )
    }

    public abstract val tableName: String
    public abstract val columnTypes: Map<String, String>

    public trait Listener {
        public fun onChanged()
    }
}


