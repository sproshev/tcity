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

package com.tcity.android.parser

import com.tcity.android.concept.BuildConfiguration
import java.io.InputStream
import java.io.InputStreamReader
import android.util.JsonReader
import java.io.IOException
import java.util.ArrayList

object BuildConfigurationsParser : ConceptsParser<BuildConfiguration> {

    override fun parse(stream: InputStream): List<BuildConfiguration> {
        val reader = JsonReader(InputStreamReader(stream))

        var result: List<BuildConfiguration>? = null
        var capacity = 10

        try {
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "count" -> capacity = reader.nextInt()
                    "buildType" -> result = readBuildConfigurations(reader, capacity)
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
        } finally {
            reader.close()
        }

        if (result != null) {
            return result!!
        } else {
            throw IOException("Invalid build configurations json: \"buildType\" is absent.")
        }
    }

    throws(javaClass<IOException>())
    private fun readBuildConfigurations(reader: JsonReader, capacity: Int): List<BuildConfiguration> {
        val result = ArrayList<BuildConfiguration>(Math.max(capacity, 0))

        reader.beginArray()

        while (reader.hasNext()) {
            result.add(readBuildConfiguration(reader))
        }

        reader.endArray()

        return result
    }

    throws(javaClass<IOException>())
    private fun readBuildConfiguration(reader: JsonReader): BuildConfiguration {
        reader.beginObject()

        var id: String? = null
        var name: String? = null
        var parentId: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "name" -> name = reader.nextString()
                "projectId" -> parentId = reader.nextString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null) {
            throw IOException("Invalid build configuration json: \"id\" is absent")
        }

        if (name == null) {
            throw IOException("Invalid build configuration json: \"name\" is absent")
        }

        if (parentId == null) {
            throw IOException("Invalid build configuration json: \"projectId\" is absent")
        }

        return BuildConfiguration(id!!, name!!, parentId!!)
    }
}
