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

package com.tcity.android.background.parser

import java.io.InputStream
import android.util.JsonReader
import java.io.IOException

public fun parseBuildConfigurations(stream: InputStream): List<BuildConfiguration> {
    return parseConcepts(stream, "buildType", ::parseBuildConfiguration)
}

throws(javaClass<IOException>())
private fun parseBuildConfiguration(reader: JsonReader): BuildConfiguration {
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