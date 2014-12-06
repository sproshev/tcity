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
import org.apache.http.HttpStatus
import android.database.sqlite.SQLiteException
import com.tcity.android.db.ConceptSchema
import com.tcity.android.db.contentValues
import com.tcity.android.concept.Project
import com.tcity.android.rest.getProjectsUrl
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.rest.getBuildConfigurationsUrl
import com.tcity.android.db.ProjectSchema
import com.tcity.android.db.BuildConfigurationSchema
import com.tcity.android.app.Preferences
import com.tcity.android.rest
import com.tcity.android.db.ANDROID_ID_COLUMN
import java.util.Collections
import com.tcity.android.concept.ROOT_PROJECT_ID
import com.tcity.android.app.DB
import java.io.InputStream
import com.tcity.android.parser.parseProjects
import com.tcity.android.parser.parseBuildConfigurations


public abstract class ConceptsRunnable<T : Concept>(
        protected val db: DB,
        protected val schema: ConceptSchema<T>,
        protected val parser: (InputStream) -> Collection<T>,
        protected val preferences: Preferences
) : Runnable {

    protected open val ignoredConceptIds: Set<String> = Collections.emptySet()

    protected fun loadAndSaveConcepts(conceptsPath: String) {
        saveConcepts(loadConcepts(conceptsPath))
    }

    throws(javaClass<IOException>(), javaClass<HttpStatusException>())
    private fun loadConcepts(url: String): Collection<T> {
        val response = rest.getJson(url, preferences.getAuth())

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return parser(response.getEntity().getContent())
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveConcepts(concepts: Collection<T>) {
        db.beginTransaction()

        try {
            db.delete(schema, null, null)

            var id = 0

            concepts.forEach {
                if (!ignoredConceptIds.contains(it.id)) {
                    val values = it.contentValues

                    values.put(ANDROID_ID_COLUMN, id)

                    db.insert(schema, values)

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
        db: DB,
        preferences: Preferences,
        private val receiver: Receiver? = null
) : ConceptsRunnable<Project>(db, ProjectSchema, ::parseProjects, preferences) {

    override val ignoredConceptIds: Set<String> = setOf(ROOT_PROJECT_ID)

    override fun run() {
        executeSafety({ loadAndSaveConcepts(getProjectsUrl(preferences)) }, receiver)
    }
}

public class BuildConfigurationsRunnable(
        private val projectId: String,
        db: DB,
        preferences: Preferences,
        private val receiver: Receiver? = null
) : ConceptsRunnable<BuildConfiguration>(db, BuildConfigurationSchema, ::parseBuildConfigurations, preferences) {

    override fun run() {
        executeSafety(
                { loadAndSaveConcepts(getBuildConfigurationsUrl(projectId, preferences)) },
                receiver
        )
    }
}
