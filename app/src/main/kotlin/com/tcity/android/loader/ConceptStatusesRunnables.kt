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
import com.tcity.android.app.Preferences
import com.tcity.android.concept.Project
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.rest.getBuildConfigurationStatusUrl
import com.tcity.android.app.DB
import com.tcity.android.db.Schema
import com.tcity.android.app.ExceptionReceiver

public abstract class ConceptStatusesRunnable<T : Concept>(
        protected val db: DB,
        protected val schema: Schema,
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
            saveStatus(conceptId, loadStatus(conceptId))
        } catch (e: Exception) {
            Log.w(LOG_TAG, e.getMessage())
        }
    }

    throws(javaClass<IOException>(), javaClass<HttpStatusException>())
    private fun loadStatus(conceptId: String): Status {
        val response = rest.getPlain(getStatusUrl(conceptId), preferences.getAuth())

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return Status.valueOf(
                    EntityUtils.toString(response.getEntity())
            )
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveStatus(conceptId: String, status: Status) {
        db.update(
                schema,
                status.contentValues,
                "${Schema.TC_ID_COLUMN} = ?",
                array(conceptId)
        )
    }

    throws(javaClass<SQLiteException>())
    private fun saveWatched(conceptId: String) {
        db.update(
                schema,
                true.contentValues(Schema.WATCHED_COLUMN),
                "${Schema.TC_ID_COLUMN} = ?",
                array(conceptId)
        )
    }
}

public class ProjectStatusesRunnable(
        db: DB,
        preferences: Preferences,
        private val exceptionReceiver: ExceptionReceiver
) : ConceptStatusesRunnable<Project>(db, Schema.PROJECT, preferences) {

    override fun getWatchedConceptIds() = preferences.getWatchedProjectIds()

    override fun getStatusUrl(conceptId: String) = getProjectStatusUrl(conceptId, preferences)

    override fun run() {
        try {
            loadAndSaveStatuses()
        } catch (e: Exception) {
            exceptionReceiver.receive(javaClass<ProjectStatusesRunnable>().getSimpleName(), e)
        }
    }
}

public class BuildConfigurationStatusesRunnable(
        db: DB,
        preferences: Preferences,
        private val exceptionReceiver: ExceptionReceiver
) : ConceptStatusesRunnable<BuildConfiguration>(db, Schema.BUILD_CONFIGURATION, preferences) {

    override fun getWatchedConceptIds() = preferences.getWatchedBuildConfigurationIds()

    override fun getStatusUrl(conceptId: String) = getBuildConfigurationStatusUrl(conceptId, preferences)

    override fun run() {
        try {
            loadAndSaveStatuses()
        } catch (e: Exception) {
            exceptionReceiver.receive(javaClass<BuildConfigurationStatusesRunnable>().getSimpleName(), e)
        }
    }
}
