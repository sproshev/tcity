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

package com.tcity.android.background.runnable

import com.tcity.android.db.DB
import com.tcity.android.background.rest.RestClient
import org.apache.http.HttpStatus
import com.tcity.android.background.HttpStatusException
import java.io.IOException
import java.util.ArrayList
import org.apache.http.util.EntityUtils
import org.apache.http.ParseException
import android.util.Log
import com.tcity.android.app.Preferences

public class FavouriteProjectsRunnable(
        private val db: DB,
        private val client: RestClient,
        private val preferences: Preferences
) : Runnable {

    throws(javaClass<IOException>(), javaClass<ParseException>())
    override fun run() {
        val newIds = loadIds(loadInternalIds()).filter { !db.isProjectFavourite(it) }

        db.addFavouriteProjects(newIds)

        loadStatusesQuietly(newIds)

        preferences.setFavouriteProjectsLastUpdate(System.currentTimeMillis())
    }

    throws(javaClass<IOException>(), javaClass<ParseException>())
    private fun loadInternalIds(): Array<String> {
        val response = client.getOverviewProjects(preferences.getLogin())

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return EntityUtils.toString(response.getEntity()).split(':')
        }
    }

    throws(javaClass<IOException>(), javaClass<ParseException>())
    private fun loadIds(internalIds: Array<String>): List<String> {
        val result = ArrayList<String>(internalIds.size())

        internalIds.forEach {
            val response = client.getProjectId(it)

            val statusLine = response.getStatusLine()

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw HttpStatusException(statusLine)
            } else {
                result.add(
                        EntityUtils.toString(response.getEntity())
                )
            }
        }

        return result
    }

    private fun loadStatusesQuietly(ids: Collection<String>) {
        ids.forEach {
            try {
                ProjectStatusRunnable(it, db, client).run()
            } catch (e: Exception) {
                Log.i(
                        javaClass<FavouriteProjectsRunnable>().getSimpleName(), e.getMessage(), e
                )
            }
        }
    }
}
