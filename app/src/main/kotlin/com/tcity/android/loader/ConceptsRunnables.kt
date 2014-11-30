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
import java.io.IOException
import com.tcity.android.parser.ConceptsParser
import org.apache.http.HttpStatus
import android.database.sqlite.SQLiteException
import com.tcity.android.db.ConceptSchema
import com.tcity.android.db.contentValues
import android.util.Log
import com.tcity.android.concept.Project
import com.tcity.android.rest.getProjectsUrl
import com.tcity.android.parser.ProjectsParser
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.rest.getBuildConfigurationsUrl
import com.tcity.android.parser.BuildConfigurationsParser
import com.tcity.android.db.DBHelper
import com.tcity.android.db.ProjectSchema
import com.tcity.android.db.BuildConfigurationSchema
import com.tcity.android.app.Preferences
import com.tcity.android.rest
import com.tcity.android.db.ANDROID_ID_COLUMN
import java.util.Collections
import com.tcity.android.concept.ROOT_PROJECT_ID
import android.os.Handler


public abstract class ConceptsRunnable<T : Concept>(
        protected val dbHelper: DBHelper,
        protected val schema: ConceptSchema<T>,
        protected val parser: ConceptsParser<T>,
        protected val preferences: Preferences
) : Runnable {

    class object {
        private val LOG_TAG = javaClass<ConceptsRunnable<*>>().getSimpleName()
    }

    protected open val ignoredConceptIds: Set<String> = Collections.emptySet()

    protected fun loadAndSaveConcepts(conceptsPath: String) {
        val concepts = loadConcepts(conceptsPath)

        Log.d(LOG_TAG, "Concepts were loaded: [table: ${schema.tableName}]")

        saveConcepts(concepts)

        Log.d(LOG_TAG, "Concepts were saved: [table: ${schema.tableName}]")
    }

    throws(javaClass<IOException>())
    private fun loadConcepts(conceptsPath: String): Collection<T> {
        val response = rest.get(
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

            var id = 0

            concepts.forEach {
                if (!ignoredConceptIds.contains(it.id)) {
                    val values = it.contentValues

                    values.put(ANDROID_ID_COLUMN, id)

                    db.insert(schema.tableName, null, values)

                    id++
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}

public class ProjectsRunnable(
        dbHelper: DBHelper,
        schema: ProjectSchema,
        parser: ProjectsParser,
        preferences: Preferences,
        private val handler: Handler? = null
) : ConceptsRunnable<Project>(dbHelper, schema, parser, preferences) {

    override val ignoredConceptIds: Set<String> = setOf(ROOT_PROJECT_ID)

    override fun run() {
        executeSafety({ loadAndSaveConcepts(getProjectsUrl()) }, handler)
    }
}

public class BuildConfigurationsRunnable(
        private val projectId: String,
        dbHelper: DBHelper,
        schema: BuildConfigurationSchema,
        parser: BuildConfigurationsParser,
        preferences: Preferences,
        private val handler: Handler? = null
) : ConceptsRunnable<BuildConfiguration>(dbHelper, schema, parser, preferences) {

    override fun run() {
        executeSafety({ loadAndSaveConcepts(getBuildConfigurationsUrl(projectId)) }, handler)
    }
}
