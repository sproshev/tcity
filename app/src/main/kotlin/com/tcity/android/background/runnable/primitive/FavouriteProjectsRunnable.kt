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

import com.tcity.android.db.DB
import com.tcity.android.background.rest.RestClient
import org.apache.http.HttpStatus
import com.tcity.android.background.runnable.HttpStatusException
import java.io.InputStream
import android.util.JsonReader
import java.io.InputStreamReader
import java.io.IOException
import java.util.ArrayList

public class FavouriteProjectsRunnable(
        private val login: String,
        private val db: DB,
        private val client: RestClient
) : Runnable {

    throws(javaClass<IOException>())
    override fun run() {
        val internalIds = loadInternalIds()

        if (internalIds != null) {
            db.addFavouriteProjects(loadIds(internalIds))
        }
    }

    throws(javaClass<IOException>())
    private fun loadInternalIds(): Array<String>? {
        val response = client.getUserProperties(login)

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            return parseInternalIds(response.getEntity().getContent())
        }
    }

    throws(javaClass<IOException>())
    private fun loadIds(internalIds: Array<String>): List<String> {
        val result = ArrayList<String>(internalIds.size())

        internalIds.forEach {
            val response = client.getProjectDetails(it)

            val statusLine = response.getStatusLine()

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw HttpStatusException(statusLine)
            } else {
                result.add(
                        parseId(
                                response.getEntity().getContent()
                        )
                )
            }
        }

        return result
    }

    throws(javaClass<IOException>())
    private fun parseInternalIds(stream: InputStream): Array<String>? {
        val reader = JsonReader(InputStreamReader(stream))

        try {
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "property" -> return getFavouriteProjectsProperty(reader)?.split(':')
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
        } finally {
            reader.close()
        }

        return array()
    }

    throws(javaClass<IOException>())
    private fun parseId(stream: InputStream): String {
        val reader = JsonReader(InputStreamReader(stream))

        try {
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> return reader.nextString()
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
        } finally {
            reader.close()
        }

        throw IOException("Invalid project details json: \"id\" is absent")
    }

    throws(javaClass<IOException>())
    private fun getFavouriteProjectsProperty(reader: JsonReader): String? {
        reader.beginArray()

        while (reader.hasNext()) {
            val property = parseProperty(reader)

            if (property.first.equals("overview.preferredProjects") && property.second != null) {
                return property.second!!
            }
        }

        reader.endArray()

        return null
    }

    throws(javaClass<IOException>())
    private fun parseProperty(reader: JsonReader): Pair<String?, String?> {
        reader.beginObject()

        var name: String? = null
        var value: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "name" -> name = reader.nextString()
                "value" -> value = reader.nextString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return name.to(value)
    }
}
