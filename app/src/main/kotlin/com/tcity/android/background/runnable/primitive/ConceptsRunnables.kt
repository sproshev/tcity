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

import java.io.IOException
import org.apache.http.HttpStatus
import android.database.sqlite.SQLiteException
import com.tcity.android.db.DB
import com.tcity.android.background.parser.parseBuildConfigurations
import com.tcity.android.background.parser.parseBuilds
import com.tcity.android.background.runnable.HttpStatusException
import com.tcity.android.background.rest.RestClient
import com.tcity.android.background.parser.parseProjects
import com.tcity.android.db.Project
import com.tcity.android.db.BuildConfiguration
import com.tcity.android.db.Build


public class ProjectsRunnable(
        private val db: DB,
        private val client: RestClient
) : Runnable {

    throws(javaClass<IOException>(), javaClass<SQLiteException>())
    override fun run() {
        db.setProjects(loadConcepts())
    }

    throws(javaClass<IOException>())
    private fun loadConcepts(): Collection<Project> {
        val response = client.getProjects()

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return parseProjects(response.getEntity().getContent())
        }
    }
}

public class BuildConfigurationsRunnable(
        private val projectId: String,
        private val db: DB,
        private val client: RestClient
) : Runnable {

    throws(javaClass<IOException>(), javaClass<SQLiteException>())
    override fun run() {
        db.setBuildConfigurations(projectId, loadConcepts())
    }

    throws(javaClass<IOException>())
    private fun loadConcepts(): Collection<BuildConfiguration> {
        val response = client.getBuildConfigurations(projectId)

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return parseBuildConfigurations(response.getEntity().getContent())
        }
    }
}

public class BuildsRunnable(
        private val buildConfigurationId: String,
        private val db: DB,
        private val client: RestClient
) : Runnable {

    throws(javaClass<IOException>(), javaClass<SQLiteException>())
    override fun run() {
        db.setBuilds(buildConfigurationId, loadConcepts())

        val currentTimeMillis = System.currentTimeMillis()

        db.setBuildConfigurationLastUpdate(buildConfigurationId, currentTimeMillis)
        db.setBuildConfigurationSyncBound(buildConfigurationId, currentTimeMillis)
    }

    throws(javaClass<IOException>())
    private fun loadConcepts(): Collection<Build> {
        val response = client.getBuilds(buildConfigurationId)

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return parseBuilds(response.getEntity().getContent())
        }
    }
}
