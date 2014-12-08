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
import com.tcity.android.db.dbValues
import com.tcity.android.concept.Project
import com.tcity.android.rest.getProjectsUrl
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.rest.getBuildConfigurationsUrl
import com.tcity.android.app.Preferences
import com.tcity.android.rest
import java.util.Collections
import com.tcity.android.concept.ROOT_PROJECT_ID
import com.tcity.android.app.DB
import java.io.InputStream
import com.tcity.android.parser.parseProjects
import com.tcity.android.parser.parseBuildConfigurations
import com.tcity.android.db.Schema
import com.tcity.android.db.dbValue
import com.tcity.android.concept.Build
import com.tcity.android.parser.parseBuilds
import com.tcity.android.rest.getBuildsUrl


public abstract class ConceptsRunnable<T : Concept>(
        protected val db: DB,
        protected val schema: Schema,
        protected val parser: (InputStream) -> Collection<T>,
        protected val preferences: Preferences
) : Runnable {

    protected abstract val url: String
    protected abstract val watchedConceptIds: Set<String>
    protected open val ignoredConceptIds: Set<String> = Collections.emptySet()

    override fun run() {
        saveConcepts(loadConcepts(url))
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
        db.set(
                schema,
                concepts
                        .stream()
                        .filter { !ignoredConceptIds.contains(it.id) }
                        .map {
                            val values = it.dbValues

                            if (watchedConceptIds.contains(it.id)) {
                                values.put(Schema.WATCHED_COLUMN, true.dbValue)
                            }

                            values
                        }
                        .toList()
        )
    }
}

public class ProjectsRunnable(
        db: DB,
        preferences: Preferences
) : ConceptsRunnable<Project>(db, Schema.PROJECT, ::parseProjects, preferences) {

    override val url = getProjectsUrl(preferences)
    override val watchedConceptIds = preferences.getWatchedProjectIds()
    override val ignoredConceptIds = setOf(ROOT_PROJECT_ID)
}

public class BuildConfigurationsRunnable(
        private val projectId: String,
        db: DB,
        preferences: Preferences
) : ConceptsRunnable<BuildConfiguration>(db, Schema.BUILD_CONFIGURATION, ::parseBuildConfigurations, preferences) {

    override val url = getBuildConfigurationsUrl(projectId, preferences)
    override val watchedConceptIds = preferences.getWatchedBuildConfigurationIds()
}

public class BuildsRunnable(
        private val buildConfigurationId: String,
        db: DB,
        preferences: Preferences
) : ConceptsRunnable<Build>(db, Schema.BUILD, ::parseBuilds, preferences) {

    override val url = getBuildsUrl(buildConfigurationId, preferences)
    override val watchedConceptIds = Collections.emptySet<String>()
}
