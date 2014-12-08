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

package com.tcity.android.loader.status

import com.tcity.android.app.DB
import com.tcity.android.db.Schema
import com.tcity.android.app.Preferences
import com.tcity.android.db.dbValue
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.rest.getBuildConfigurationStatusUrl
import com.tcity.android.db.getId
import android.util.Log


public abstract class WatchedConceptStatusesRunnable(
        private val db: DB,
        private val schema: Schema,
        protected val preferences: Preferences
) : Runnable {

    class object {
        private val LOG_TAG = javaClass<WatchedConceptStatusesRunnable>().getSimpleName()
    }

    protected abstract fun getStatusUrl(conceptId: String): String

    override fun run() {
        val cursor = db.query(
                schema,
                array(Schema.TC_ID_COLUMN),
                "${Schema.WATCHED_COLUMN} = ?",
                array(true.dbValue.toString())
        )

        while (cursor.moveToNext()) {
            val id = getId(cursor)

            try {
                saveStatus(
                        id,
                        loadStatus(getStatusUrl(id), preferences),
                        db,
                        schema
                )
            } catch (e: Exception) {
                Log.w(LOG_TAG, e.getMessage())
            }
        }

        cursor.close()
    }
}

public class WatchedProjectStatusesRunnable(
        db: DB,
        preferences: Preferences
) : WatchedConceptStatusesRunnable(db, Schema.PROJECT, preferences) {

    override fun getStatusUrl(conceptId: String) = getProjectStatusUrl(conceptId, preferences)
}

public class WatchedBuildConfigurationStatusesRunnable(
        db: DB,
        preferences: Preferences
) : WatchedConceptStatusesRunnable(db, Schema.BUILD_CONFIGURATION, preferences) {

    override fun getStatusUrl(conceptId: String) = getBuildConfigurationStatusUrl(conceptId, preferences)
}
