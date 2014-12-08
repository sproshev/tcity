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

import java.io.IOException
import com.tcity.android.concept.Status
import com.tcity.android.rest
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import android.database.sqlite.SQLiteException
import com.tcity.android.db.dbValues
import com.tcity.android.db.Schema
import com.tcity.android.app.Preferences
import com.tcity.android.app.DB
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.rest.getBuildConfigurationStatusUrl

public fun getProjectStatusRunnable(
        id: String,
        db: DB,
        preferences: Preferences
): Runnable = ConceptStatusRunnable(id, getProjectStatusUrl(id, preferences), db, preferences)

public fun getBuildConfigurationStatusRunnable(
        id: String,
        db: DB,
        preferences: Preferences
): Runnable = ConceptStatusRunnable(id, getBuildConfigurationStatusUrl(id, preferences), db, preferences)

private class ConceptStatusRunnable(
        private val id: String,
        private val url: String,
        private val db: DB,
        private val preferences: Preferences
) : Runnable {

    override fun run() {
        saveStatus(
                id,
                loadStatus(url, preferences),
                db,
                Schema.PROJECT
        )
    }

    throws(javaClass<IOException>(), javaClass<HttpStatusException>())
    private fun loadStatus(statusUrl: String, preferences: Preferences): Status {
        val response = rest.getPlain(
                statusUrl,
                preferences.getAuth()
        )

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
    private fun saveStatus(conceptId: String, status: Status, db: DB, schema: Schema) {
        db.update(
                schema,
                status.dbValues,
                "${Schema.TC_ID_COLUMN} = ?",
                array(conceptId)
        )
    }
}