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

import java.io.IOException
import com.tcity.android.Status
import com.tcity.android.rest
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import android.database.sqlite.SQLiteException
import com.tcity.android.db.Schema
import com.tcity.android.app.Preferences
import com.tcity.android.app.DB
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.rest.getBuildConfigurationStatusUrl
import com.tcity.android.db.CVUtils

public fun getProjectStatusRunnable(
        id: String,
        db: DB,
        preferences: Preferences
): Runnable = ConceptStatusRunnable(
        id,
        getProjectStatusUrl(id, preferences),
        preferences,
        db,
        Schema.PROJECT
)

public fun getBuildConfigurationStatusRunnable(
        id: String,
        db: DB,
        preferences: Preferences
): Runnable = ConceptStatusRunnable(
        id,
        getBuildConfigurationStatusUrl(id, preferences),
        preferences,
        db,
        Schema.BUILD_CONFIGURATION
)

private class ConceptStatusRunnable(
        private val id: String,
        private val url: String,
        private val preferences: Preferences,
        private val db: DB,
        private val schema: Schema
) : Runnable {

    override fun run() {
        saveStatus(loadStatus())
    }

    throws(javaClass<IOException>(), javaClass<HttpStatusException>())
    private fun loadStatus(): Status {
        val response = rest.getPlain(url, preferences.getAuth())

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