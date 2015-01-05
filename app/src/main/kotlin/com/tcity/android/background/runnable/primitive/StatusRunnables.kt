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
import com.tcity.android.db.DB
import com.tcity.android.background.runnable.HttpStatusException
import com.tcity.android.background.rest.RestClient


public class ProjectStatusRunnable(
        private val id: String,
        private val db: DB,
        private val client: RestClient
) : Runnable {

    throws(javaClass<IOException>(), javaClass<SQLiteException>())
    override fun run() {
        db.setProjectStatus(id, loadStatus())
    }

    throws(javaClass<IOException>())
    private fun loadStatus(): Status {
        val response = client.getProjectStatus(id)

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return Status.valueOf(
                    EntityUtils.toString(response.getEntity())
            )
        }
    }
}

public class BuildConfigurationStatusRunnable(
        private val id: String,
        private val db: DB,
        private val client: RestClient
) : Runnable {

    throws(javaClass<IOException>(), javaClass<SQLiteException>())
    override fun run() {
        db.setBuildConfigurationStatus(id, loadStatus())
    }

    throws(javaClass<IOException>())
    private fun loadStatus(): Status {
        val response = client.getBuildConfigurationStatus(id)

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return Status.valueOf(
                    EntityUtils.toString(response.getEntity())
            )
        }
    }
}