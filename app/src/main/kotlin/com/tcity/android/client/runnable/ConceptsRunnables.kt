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

package com.tcity.android.client.runnable

import com.tcity.android.client.parser.Concept
import java.io.IOException
import org.apache.http.HttpStatus
import android.database.sqlite.SQLiteException
import com.tcity.android.rest.getProjectsUrl
import com.tcity.android.rest.getBuildConfigurationsUrl
import com.tcity.android.app.Preferences
import com.tcity.android.rest
import java.util.Collections
import com.tcity.android.app.DB
import java.io.InputStream
import com.tcity.android.client.parser.parseProjects
import com.tcity.android.client.parser.parseBuildConfigurations
import com.tcity.android.db.Schema
import com.tcity.android.client.parser.parseBuilds
import com.tcity.android.rest.getBuildsUrl
import java.util.ArrayList
import android.content.ContentValues
import java.util.HashMap
import com.tcity.android.db.CVUtils
import com.tcity.android.db.DBUtils
import com.tcity.android.db.SelectionUtils
import com.tcity.android.ROOT_PROJECT_ID
import com.tcity.android.Status

public fun getProjectsRunnable(
        db: DB,
        preferences: Preferences
): Runnable = ConceptsRunnable(
        getProjectsUrl(preferences),
        preferences,
        ::parseProjects,
        db,
        Schema.PROJECT,
        null,
        setOf(ROOT_PROJECT_ID)
)

public fun getBuildConfigurationsRunnable(
        projectId: String,
        db: DB,
        preferences: Preferences
): Runnable = ConceptsRunnable(
        getBuildConfigurationsUrl(projectId, preferences),
        preferences,
        ::parseBuildConfigurations,
        db,
        Schema.BUILD_CONFIGURATION,
        projectId
)

public fun getBuildsRunnable(
        buildConfigurationId: String,
        db: DB,
        preferences: Preferences
): Runnable = ConceptsRunnable(
        getBuildsUrl(buildConfigurationId, preferences),
        preferences,
        ::parseBuilds,
        db,
        Schema.BUILD,
        buildConfigurationId
)

private class ConceptsRunnable<T : Concept>(
        private val url: String,
        private val preferences: Preferences,
        private val parser: (InputStream) -> Collection<T>,
        private val db: DB,
        private val schema: Schema,
        private val parentId: String?,
        private val ignoredConceptIds: Set<String> = Collections.emptySet()
) : Runnable {

    override fun run() {
        saveConcepts(loadConcepts())
    }

    throws(javaClass<IOException>(), javaClass<HttpStatusException>())
    private fun loadConcepts(): Collection<T> {
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
        val favouriteIdToStatus = loadFavouriteIdToStatus()
        val result = ArrayList<ContentValues>()

        concepts.forEach {
            if (!ignoredConceptIds.contains(it.id)) {
                val values = CVUtils.toContentValues(it)

                if (favouriteIdToStatus.contains(it.id)) {
                    values.putAll(CVUtils.toFavouriteContentValues(true))

                    if (it.status == Status.DEFAULT) {
                        values.putAll(
                                CVUtils.toContentValues(
                                        favouriteIdToStatus.get(it.id)!!
                                )
                        )
                    }
                }

                result.add(values)
            }
        }

        db.set(schema, result, parentId)
    }

    throws(javaClass<SQLiteException>())
    private fun loadFavouriteIdToStatus(): Map<String, Status> {
        val result = HashMap<String, Status>()

        val cursor = db.query(
                schema,
                array(Schema.TC_ID_COLUMN, Schema.STATUS_COLUMN),
                SelectionUtils.getSelection(parentId, Schema.PARENT_ID_COLUMN, Schema.FAVOURITE_COLUMN),
                SelectionUtils.getSelectionArgs(parentId, CVUtils.toFavouriteContentValue(true))
        )

        while (cursor.moveToNext()) {
            result.put(
                    DBUtils.getId(cursor),
                    DBUtils.getStatus(cursor)
            )
        }

        cursor.close()

        return result
    }
}
