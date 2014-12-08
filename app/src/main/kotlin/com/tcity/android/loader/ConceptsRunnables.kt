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
import com.tcity.android.rest.getProjectsUrl
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
import com.tcity.android.parser.parseBuilds
import com.tcity.android.rest.getBuildsUrl
import com.tcity.android.concept.Status
import java.util.ArrayList
import android.content.ContentValues
import java.util.HashMap
import com.tcity.android.db.getId
import com.tcity.android.db.getStatus

public fun getProjectsRunnable(
        db: DB,
        preferences: Preferences
): Runnable {
    return ConceptsRunnable(
            getProjectsUrl(preferences),
            null,
            ::parseProjects,
            db,
            Schema.PROJECT,
            preferences,
            setOf(ROOT_PROJECT_ID)
    )
}

public fun getBuildConfigurationsRunnable(
        projectId: String,
        db: DB,
        preferences: Preferences
): Runnable {
    return ConceptsRunnable(
            getBuildConfigurationsUrl(projectId, preferences),
            projectId,
            ::parseBuildConfigurations,
            db,
            Schema.BUILD_CONFIGURATION,
            preferences
    )
}

public fun getBuildsRunnable(
        buildConfigurationId: String,
        db: DB,
        preferences: Preferences
): Runnable {
    return ConceptsRunnable(
            getBuildsUrl(buildConfigurationId, preferences),
            buildConfigurationId,
            ::parseBuilds,
            db,
            Schema.BUILD,
            preferences
    )
}

private class ConceptsRunnable<T : Concept>(
        private val url: String,
        private val parentId: String?,
        private val parser: (InputStream) -> Collection<T>,
        private val db: DB,
        private val schema: Schema,
        private val preferences: Preferences,
        private val ignoredConceptIds: Set<String> = Collections.emptySet()
) : Runnable {

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
        val watchedIdToStatus = loadWatchedIdToStatus()
        val result = ArrayList<ContentValues>()

        concepts.forEach {
            if (!ignoredConceptIds.contains(it.id)) {
                val values = it.dbValues

                if (watchedIdToStatus.contains(it.id)) {
                    values.put(Schema.WATCHED_COLUMN, true.dbValue)

                    if (it.status == Status.DEFAULT) {
                        values.put(Schema.STATUS_COLUMN, watchedIdToStatus.get(it.id)!!.dbValue)
                    }
                }

                result.add(values)
            }
        }

        db.set(schema, result, parentId)
    }

    throws(javaClass<SQLiteException>())
    private fun loadWatchedIdToStatus(): Map<String, Status> {
        val result = HashMap<String, Status>()

        val cursor = db.query(
                schema,
                array(Schema.TC_ID_COLUMN, Schema.STATUS_COLUMN),
                calculateSelection(),
                calculateSelectionArgs()
        )

        while (cursor.moveToNext()) {
            result.put(
                    getId(cursor),
                    getStatus(cursor)
            )
        }

        cursor.close()

        return result
    }

    private fun calculateSelection(): String {
        return if (parentId == null) {
            "${Schema.WATCHED_COLUMN} = ?"
        } else {
            "${Schema.WATCHED_COLUMN} = ? AND ${Schema.PARENT_ID_COLUMN} = ?"
        }
    }

    private fun calculateSelectionArgs(): Array<String> {
        return if (parentId == null) {
            array(true.dbValue.toString())
        } else {
            array(true.dbValue.toString(), parentId)
        }
    }
}
