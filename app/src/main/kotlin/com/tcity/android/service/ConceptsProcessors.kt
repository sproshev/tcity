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

package com.tcity.android.service

import com.tcity.android.concept.Concept
import android.content.Context
import com.tcity.android.Application
import java.io.IOException
import com.tcity.android.parser.ConceptsParser
import com.tcity.android.rest.get
import org.apache.http.HttpStatus
import android.database.sqlite.SQLiteException
import com.tcity.android.db.ConceptSchema
import com.tcity.android.db.contentValues
import android.util.Log
import com.tcity.android.concept.Status
import org.apache.http.util.EntityUtils
import com.tcity.android.concept.Project
import com.tcity.android.db.ProjectDBHelper
import android.content.Intent
import com.tcity.android.rest.getProjectsUrl
import com.tcity.android.parser.ProjectsParser
import com.tcity.android.db.PROJECT_SCHEMA
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.db.BuildConfigurationDBHelper
import com.tcity.android.INTENT_PROJECT_ID_KEY
import com.tcity.android.rest.getBuildConfigurationsUrl
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.db.BUILD_CONFIGURATION_SCHEMA
import com.tcity.android.rest.getBuildConfigurationStatusUrl
import org.apache.http.StatusLine
import com.tcity.android.db.ConceptDBHelper


public abstract class ConceptsProcessor<T : Concept>(protected val preferences: Application.Preferences) {

    class object {
        private val LOG_TAG = javaClass<ConceptsProcessor<*>>().getSimpleName()
    }

    protected abstract val dbHelper: ConceptDBHelper<T>
    protected abstract val parser: ConceptsParser<T>
    protected abstract val schema: ConceptSchema<T>

    protected abstract fun getWatchedConceptIds(): Set<String>
    protected abstract fun getStatusUrl(conceptId: String): String

    throws(javaClass<Exception>())
    protected abstract fun run(intent: Intent)

    protected fun loadAndSaveConcepts(conceptsPath: String) {
        saveConcepts(loadConcepts(conceptsPath))
    }

    protected fun loadAndSaveStatuses() {
        getWatchedConceptIds().forEach { loadAndSaveStatus(it) }
    }

    throws(javaClass<IOException>())
    private fun loadConcepts(conceptsPath: String): Collection<T> {
        val response = get(
                preferences.getUrl() + conceptsPath,
                preferences.getAuth()
        )

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw IOException(getStatusLineExceptionMessage(statusLine))
        } else {
            return parser.parse(response.getEntity().getContent())
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveConcepts(concepts: Collection<T>) {
        val db = dbHelper.getWritableDatabase()

        db.beginTransaction()

        try {
            db.delete(schema.tableName, null, null)

            concepts.forEach {
                db.insert(schema.tableName, null, it.contentValues)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun loadAndSaveStatus(conceptId: String) {
        try {
            saveStatus(conceptId, loadStatus(conceptId))
        } catch (e: Exception) {
            Log.w(LOG_TAG, e.getMessage())
        }
    }

    throws(javaClass<IOException>())
    private fun loadStatus(conceptId: String): Status {
        val response = get(
                preferences.getUrl() + getStatusUrl(conceptId),
                preferences.getAuth()
        )

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw IOException(getStatusLineExceptionMessage(statusLine))
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

    private fun getStatusLineExceptionMessage(statusLine: StatusLine): String {
        return "${statusLine.getStatusCode()} ${statusLine.getReasonPhrase()}"
    }
}

public class ProjectsProcessor(context: Context, preferences: Application.Preferences) : ConceptsProcessor<Project>(preferences) {

    override val dbHelper = ProjectDBHelper(context)
    override val parser = ProjectsParser()
    override val schema = PROJECT_SCHEMA

    override fun getWatchedConceptIds() = preferences.getWatchedProjectIds()

    override fun getStatusUrl(conceptId: String) = getProjectStatusUrl(conceptId)

    throws(javaClass<Exception>())
    override fun run(intent: Intent) {
        loadAndSaveConcepts(getProjectsUrl())
        loadAndSaveStatuses()
    }
}

public class BuildConfigurationsProcessor(context: Context, preferences: Application.Preferences) : ConceptsProcessor<BuildConfiguration>(preferences) {

    class object {
        private val LOG_TAG = javaClass<BuildConfigurationsProcessor>().getSimpleName()
    }

    override val dbHelper = BuildConfigurationDBHelper(context)
    override val parser = BuildConfigurationsParser()
    override val schema = BUILD_CONFIGURATION_SCHEMA

    override fun getWatchedConceptIds() = preferences.getWatchedBuildConfigurationsIds()

    override fun getStatusUrl(conceptId: String) = getBuildConfigurationStatusUrl(conceptId)

    throws(javaClass<Exception>())
    override fun run(intent: Intent) {
        val projectId = intent.getStringExtra(INTENT_PROJECT_ID_KEY)

        if (projectId != null) {
            loadAndSaveConcepts(getBuildConfigurationsUrl(projectId))
            loadAndSaveStatuses()
        } else {
            Log.w(LOG_TAG, "Invalid intent: \"projectId\" is absent.")
        }
    }
}
