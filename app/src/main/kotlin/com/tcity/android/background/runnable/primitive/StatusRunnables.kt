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
import com.tcity.android.Status
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import android.database.sqlite.SQLiteException
import com.tcity.android.db.Schema
import com.tcity.android.app.DB
import com.tcity.android.db.CVUtils
import com.tcity.android.background.runnable.HttpStatusException
import org.apache.http.HttpResponse
import com.tcity.android.background.rest.RestClient


public class ProjectStatusRunnable(
        id: String,
        db: DB,
        private val client: RestClient
) : ProjectOrBuildConfigurationStatusRunnable(id, db, Schema.PROJECT) {

    throws(javaClass<IOException>())
    override fun getHttpResponse(): HttpResponse = client.getProjectStatus(id)
}

public class BuildConfigurationStatusRunnable(
        id: String,
        db: DB,
        private val client: RestClient
) : ProjectOrBuildConfigurationStatusRunnable(id, db, Schema.BUILD_CONFIGURATION) {

    throws(javaClass<IOException>())
    override fun getHttpResponse(): HttpResponse = client.getBuildConfigurationStatus(id)
}

private abstract class ProjectOrBuildConfigurationStatusRunnable(
        protected val id: String,
        private val db: DB,
        private val schema: Schema
) : Runnable {

    throws(javaClass<IOException>(), javaClass<SQLiteException>())
    override fun run() {
        saveStatus(loadStatus())
    }

    throws(javaClass<IOException>())
    protected abstract fun getHttpResponse(): HttpResponse

    throws(javaClass<IOException>())
    private fun loadStatus(): Status {
        val response = getHttpResponse()

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return Status.valueOf(
                    EntityUtils.toString(response.getEntity())
            )
        }
    }

    throws(javaClass<SQLiteException>())
    private fun saveStatus(status: Status) {
        db.update(
                schema,
                CVUtils.toContentValues(status),
                "${Schema.TC_ID_COLUMN} = ?",
                array(id)
        )
    }
}