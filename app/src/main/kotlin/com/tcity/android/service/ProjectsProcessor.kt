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

import android.content.Intent
import com.tcity.android.rest.get
import com.tcity.android.rest.getProjectsUrl
import com.tcity.android.Application
import com.tcity.android.parser.parseProjects
import org.apache.http.HttpStatus
import java.io.IOException
import android.content.Context
import com.tcity.android.db.ProjectDBHelper
import com.tcity.android.db.contentValues
import com.tcity.android.db.PROJECT_SCHEMA
import com.tcity.android.concept.Project
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.concept.Status
import org.apache.http.util.EntityUtils
import android.database.sqlite.SQLiteException
import android.util.Log

public class ProjectsProcessor(context: Context, preferences: Application.Preferences) : Processor(context, preferences) {

    class object {
        private val LOG_TAG = javaClass<ProjectsProcessor>().getSimpleName()
    }

    private val dbHelper = ProjectDBHelper(context)

    throws(javaClass<Exception>())
    override fun run(intent: Intent) {
        val projects = loadProjects()

        saveProjects(projects)

        loadAndSaveStatuses(projects)
    }

    throws(javaClass<IOException>())
    private fun loadProjects(): Collection<Project> {
        val response = get(
                preferences.getUrl() + getProjectsUrl(),
                preferences.getAuth()
        )

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw IOException(getMessage(statusLine))
        } else {
            return parseProjects(response.getEntity().getContent())
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveProjects(projects: Collection<Project>) {
        val db = dbHelper.getWritableDatabase()

        db.beginTransaction()

        try {
            db.delete(PROJECT_SCHEMA.tableName, null, null)

            projects.forEach {
                db.insert(PROJECT_SCHEMA.tableName, null, it.contentValues)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun loadAndSaveStatuses(projects: Collection<Project>) {
        val watchedProjectIds = preferences.getWatchedProjectIds()

        projects
                .stream()
                .filter { watchedProjectIds.contains(it.id) }
                .forEach { loadAndSaveStatus(it) }
    }

    private fun loadAndSaveStatus(project: Project) {
        try {
            saveStatus(project, loadStatus(project.id))
        } catch (e: Exception) {
            Log.w(LOG_TAG, e.getMessage())
        }
    }

    throws(javaClass<IOException>())
    private fun loadStatus(projectId: String): Status {
        val response = get(
                preferences.getUrl() + getProjectStatusUrl(projectId),
                preferences.getAuth()
        )

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw IOException(getMessage(statusLine))
        } else {
            return Status.valueOf(
                    EntityUtils.toString(response.getEntity())
            )
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveStatus(project: Project, status: Status) {
        dbHelper.getWritableDatabase().update(
                PROJECT_SCHEMA.tableName,
                status.contentValues,
                "id = ?",
                array(project.id)
        )
    }
}
