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

package com.tcity.android.background.runnable.primitive

import com.tcity.android.background.parser.Concept
import java.io.IOException
import org.apache.http.HttpStatus
import android.database.sqlite.SQLiteException
import java.util.Collections
import com.tcity.android.app.DB
import java.io.InputStream
import com.tcity.android.background.parser.parseBuildConfigurations
import com.tcity.android.db.Schema
import com.tcity.android.background.parser.parseBuilds
import java.util.ArrayList
import android.content.ContentValues
import java.util.HashMap
import com.tcity.android.db.CVUtils
import com.tcity.android.db.DBUtils
import com.tcity.android.db.SelectionUtils
import com.tcity.android.ROOT_PROJECT_ID
import com.tcity.android.Status
import com.tcity.android.background.runnable.HttpStatusException
import org.apache.http.HttpResponse
import com.tcity.android.background.parser.Build
import com.tcity.android.background.rest.RestClient
import com.tcity.android.background.parser.BuildConfiguration
import com.tcity.android.background.parser.Project
import com.tcity.android.background.parser.parseProjects


public class ProjectsRunnable(
        db: DB,
        private val client: RestClient
) : ConceptsRunnable<Project>(null, db, Schema.PROJECT, setOf(ROOT_PROJECT_ID)) {

    throws(javaClass<IOException>())
    override fun getHttpResponse() = client.getProjects()

    throws(javaClass<IOException>())
    override fun parseConcepts(stream: InputStream) = parseProjects(stream)
}

public class BuildConfigurationsRunnable(
        projectId: String,
        db: DB,
        private val client: RestClient
) : ConceptsRunnable<BuildConfiguration>(projectId, db, Schema.BUILD_CONFIGURATION) {

    throws(javaClass<IOException>())
    override fun getHttpResponse(): HttpResponse = client.getBuildConfigurations(parentId)

    throws(javaClass<IOException>())
    override fun parseConcepts(stream: InputStream) = parseBuildConfigurations(stream)
}

public class BuildsRunnable(
        buildConfigurationId: String,
        db: DB,
        private val client: RestClient
) : ConceptsRunnable<Build>(buildConfigurationId, db, Schema.BUILD) {

    throws(javaClass<IOException>())
    override fun getHttpResponse() = client.getBuilds(parentId)

    throws(javaClass<IOException>())
    override fun parseConcepts(stream: InputStream) = parseBuilds(stream)
}

private abstract class ConceptsRunnable<T : Concept>(
        protected val parentId: String?,
        private val db: DB,
        private val schema: Schema,
        private val ignoredConceptIds: Set<String> = Collections.emptySet()
) : Runnable {

    throws(javaClass<IOException>(), javaClass<SQLiteException>())
    override fun run() {
        saveConcepts(loadConcepts())
    }

    throws(javaClass<IOException>())
    protected abstract fun getHttpResponse(): HttpResponse

    throws(javaClass<IOException>())
    protected abstract fun parseConcepts(stream: InputStream): Collection<T>

    throws(javaClass<IOException>())
    private fun loadConcepts(): Collection<T> {
        val response = getHttpResponse()

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return parseConcepts(response.getEntity().getContent())
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
