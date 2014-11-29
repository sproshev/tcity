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
import com.tcity.android.rest.getProjectsUrl
import com.tcity.android.parser.ProjectsParser
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.rest.getBuildConfigurationsUrl
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.rest.getBuildConfigurationStatusUrl
import org.apache.http.StatusLine
import com.tcity.android.parser.BuildConfigurationsParser
import com.tcity.android.db.DBHelper
import com.tcity.android.db.ProjectSchema
import com.tcity.android.db.BuildConfigurationSchema
import android.os.AsyncTask


public abstract class ConceptsTask<T : Concept>(
        protected val dbHelper: DBHelper,
        protected val schema: ConceptSchema<T>,
        protected val parser: ConceptsParser<T>,
        protected val preferences: Application.Preferences
) : AsyncTask<Void, Void, Void>() {

    class object {
        private val LOG_TAG = javaClass<ConceptsTask<*>>().getSimpleName()
    }

    protected abstract fun getWatchedConceptIds(): Set<String>
    protected abstract fun getStatusUrl(conceptId: String): String

    protected fun loadAndSaveConcepts(conceptsPath: String) {
        val concepts = loadConcepts(conceptsPath)

        Log.d(LOG_TAG, "Concepts were loaded: [table: ${schema.tableName}]")

        saveConcepts(concepts)

        Log.d(LOG_TAG, "Concepts were saved: [table: ${schema.tableName}]")
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
            throw IOException(statusLine.exceptionMessage)
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
        val response = get(
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

    private val StatusLine.exceptionMessage: String
        get() = "${getStatusCode()} ${getReasonPhrase()}"
}

public class ProjectsTask(
        dbHelper: DBHelper,
        schema: ProjectSchema,
        parser: ProjectsParser,
        preferences: Application.Preferences
) : ConceptsTask<Project>(dbHelper, schema, parser, preferences) {

    override fun getWatchedConceptIds() = preferences.getWatchedProjectIds()

    override fun getStatusUrl(conceptId: String) = getProjectStatusUrl(conceptId)

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            loadAndSaveConcepts(getProjectsUrl())
            loadAndSaveStatuses()

            // TODO send
        } catch (e: Exception) {
            // TODO send
        }

        return null
    }
}

public class BuildConfigurationsTask(
        private val projectId: String,
        dbHelper: DBHelper,
        schema: BuildConfigurationSchema,
        parser: BuildConfigurationsParser,
        preferences: Application.Preferences
) : ConceptsTask<BuildConfiguration>(dbHelper, schema, parser, preferences) {

    override fun getWatchedConceptIds() = preferences.getWatchedBuildConfigurationsIds()

    override fun getStatusUrl(conceptId: String) = getBuildConfigurationStatusUrl(conceptId)

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            loadAndSaveConcepts(getBuildConfigurationsUrl(projectId))
            loadAndSaveStatuses()

            // TODO send
        } catch (e: Exception) {
            // TODO send
        }

        return null
    }
}
