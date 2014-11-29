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

import com.tcity.android.Application
import android.content.Context
import android.content.Intent
import com.tcity.android.INTENT_PROJECT_ID_KEY
import android.util.Log
import com.tcity.android.db.BuildConfigurationDBHelper
import java.io.IOException
import com.tcity.android.rest.get
import org.apache.http.HttpStatus
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.rest.getBuildConfigurationsUrl
import android.database.sqlite.SQLiteException
import com.tcity.android.db.contentValues
import com.tcity.android.db.BUILD_CONFIGURATION_SCHEMA
import com.tcity.android.concept.Status
import org.apache.http.util.EntityUtils
import com.tcity.android.rest.getBuildConfigurationStatusUrl

public class BuildConfigurationsProcessor(context: Context, preferences: Application.Preferences) : Processor(context, preferences) {

    class object {
        private val LOG_TAG = javaClass<BuildConfigurationsProcessor>().getSimpleName()
    }

    private val dbHelper = BuildConfigurationDBHelper(context)

    throws(javaClass<Exception>())
    override fun run(intent: Intent) {
        val projectId = intent.getStringExtra(INTENT_PROJECT_ID_KEY)

        if (projectId != null) {
            val buildConfigurations = loadBuildConfigurations(projectId)

            saveBuildConfigurations(buildConfigurations)

            loadAndSaveStatuses(buildConfigurations)
        } else {
            Log.w(LOG_TAG, "Invalid intent: \"projectId\" is absent.")
        }
    }

    throws(javaClass<IOException>())
    private fun loadBuildConfigurations(projectId: String): Collection<BuildConfiguration> {
        val response = get(
                preferences.getUrl() + getBuildConfigurationsUrl(projectId),
                preferences.getAuth()
        )

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw IOException(getMessage(statusLine))
        } else {
            return parseBuildConfigurations(response.getEntity().getContent())
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveBuildConfigurations(buildConfigurations: Collection<BuildConfiguration>) {
        val db = dbHelper.getWritableDatabase()

        db.beginTransaction()

        try {
            db.delete(BUILD_CONFIGURATION_SCHEMA.tableName, null, null)

            buildConfigurations.forEach {
                db.insert(BUILD_CONFIGURATION_SCHEMA.tableName, null, it.contentValues)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun loadAndSaveStatuses(buildConfigurations: Collection<BuildConfiguration>) {
        val watchedBuildConfigurationIds = preferences.getWatchedBuildConfigurationsIds()

        buildConfigurations
                .stream()
                .filter { watchedBuildConfigurationIds.contains(it.id) }
                .forEach { loadAndSaveStatus(it) }
    }

    private fun loadAndSaveStatus(buildConfiguration: BuildConfiguration) {
        try {
            saveStatus(buildConfiguration, loadStatus(buildConfiguration.id))
        } catch (e: Exception) {
            Log.w(LOG_TAG, e.getMessage())
        }
    }

    throws(javaClass<IOException>())
    private fun loadStatus(buildConfigurationId: String): Status {
        val response = get(
                preferences.getUrl() + getBuildConfigurationStatusUrl(buildConfigurationId),
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
    private fun saveStatus(buildConfiguration: BuildConfiguration, status: Status) {
        dbHelper.getWritableDatabase().update(
                BUILD_CONFIGURATION_SCHEMA.tableName,
                status.contentValues,
                "id = ?",
                array(buildConfiguration.id)
        )
    }
}
