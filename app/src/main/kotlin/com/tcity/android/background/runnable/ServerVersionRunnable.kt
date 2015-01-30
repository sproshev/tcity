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

import com.tcity.android.app.Preferences
import com.tcity.android.background.rest.RestClient
import java.io.IOException
import org.apache.http.HttpStatus
import com.tcity.android.background.HttpStatusException
import android.util.JsonReader
import java.io.InputStreamReader
import java.io.InputStream

public class ServerVersionRunnable(
        private val client: RestClient,
        private val preferences: Preferences
) : Runnable {

    throws(javaClass<IOException>())
    override fun run() {
        val response = client.getServer()

        val statusLine = response.getStatusLine()

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw HttpStatusException(statusLine)
        } else {
            parseAndSave(response.getEntity().getContent())
        }
    }

    throws(javaClass<IOException>())
    private fun parseAndSave(stream: InputStream) {
        val reader = JsonReader(InputStreamReader(stream))

        try {
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "version" -> preferences.setServerVersion(reader.nextString())
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
        } finally {
            reader.close()
        }
    }
}
