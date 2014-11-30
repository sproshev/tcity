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

package com.tcity.android.loader

import com.tcity.android.concept.Concept
import android.util.Log
import java.io.IOException
import com.tcity.android.concept.Status
import com.tcity.android.rest
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import android.database.sqlite.SQLiteException
import com.tcity.android.db.contentValues
import com.tcity.android.db.WATCHED_COLUMN
import com.tcity.android.db.ConceptSchema
import com.tcity.android.db.DBHelper
import com.tcity.android.app.Preferences
import com.tcity.android.db.ProjectSchema
import com.tcity.android.concept.Project
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.db.BuildConfigurationSchema
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.rest.getBuildConfigurationStatusUrl
import android.os.Handler

public abstract class ConceptStatusesRunnable<T : Concept>(
        protected val dbHelper: DBHelper,
        protected val schema: ConceptSchema<T>,
        protected val preferences: Preferences
) : Runnable {

    class object {
        private val LOG_TAG = javaClass<ConceptStatusesRunnable<*>>().getSimpleName()
    }

    protected abstract fun getWatchedConceptIds(): Set<String>
    protected abstract fun getStatusUrl(conceptId: String): String

    protected fun loadAndSaveStatuses() {
        getWatchedConceptIds().forEach {
            loadAndSaveStatus(it)
            saveWatched(it)
        }
    }

    private fun loadAndSaveStatus(conceptId: String) {
        try {
            val status = loadStatus(conceptId)

            Log.d(LOG_TAG, "Status was loaded: [table: ${schema.tableName}, id: $conceptId, status: $status]")

            saveStatus(conceptId, status)

            Log.d(LOG_TAG, "Status was saved: [table: ${schema.tableName}, id: $conceptId, status: $status]")
        } catch (e: Exception) {
            Log.w(LOG_TAG, e.getMessage())
        }
    }

    throws(javaClass<IOException>())
    private fun loadStatus(conceptId: String): Status {
        val response = rest.get(
                preferences.getUrl() + getStatusUrl(conceptId),
                preferences.getAuth()
        )

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw IOException(statusLine.exceptionMessage)
        } else {
            return Status.valueOf(
                    EntityUtils.toString(response.getEntity())
            )
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveStatus(conceptId: String, status: Status) {
        dbHelper.getWritableDatabase().update(
                schema.tableName,
                status.contentValues,
                "id = ?",
                array(conceptId)
        )
    }

    throws(javaClass<SQLiteException>())
    private fun saveWatched(conceptId: String) {
        dbHelper.getWritableDatabase().update(
                schema.tableName,
                true.contentValues(WATCHED_COLUMN),
                "id = ?",
                array(conceptId)
        )
    }
}

public class ProjectStatusesRunnable(
        dbHelper: DBHelper,
        schema: ProjectSchema,
        preferences: Preferences,
        private val handler: Handler? = null
) : ConceptStatusesRunnable<Project>(dbHelper, schema, preferences) {

    override fun getWatchedConceptIds() = preferences.getWatchedProjectIds()

    override fun getStatusUrl(conceptId: String) = getProjectStatusUrl(conceptId)

    override fun run() {
        executeSafety({ loadAndSaveStatuses() }, handler)
    }
}

public class BuildConfigurationStatusesRunnable(
        dbHelper: DBHelper,
        schema: BuildConfigurationSchema,
        preferences: Preferences,
        private val handler: Handler? = null
) : ConceptStatusesRunnable<BuildConfiguration>(dbHelper, schema, preferences) {

    override fun getWatchedConceptIds() = preferences.getWatchedBuildConfigurationIds()

    override fun getStatusUrl(conceptId: String) = getBuildConfigurationStatusUrl(conceptId)

    override fun run() {
        executeSafety({ loadAndSaveStatuses() }, handler)
    }
}
