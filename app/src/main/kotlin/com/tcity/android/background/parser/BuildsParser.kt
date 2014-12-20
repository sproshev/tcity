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
import com.tcity.android.Status

throws(javaClass<IOException>())
public fun parseBuilds(stream: InputStream): List<Build> {
    return parseConcepts(stream, "build", ::parseBuild)
}

throws(javaClass<IOException>())
private fun parseBuild(reader: JsonReader): Build {
    reader.beginObject()

    var id: String? = null
    var name: String? = null
    var parentId: String? = null
    var status: Status? = null

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "id" -> id = reader.nextString()
            "number" -> name = reader.nextString()
            "buildTypeId" -> parentId = reader.nextString()
            "status" -> status = Status.valueOf(reader.nextString())
            else -> reader.skipValue()
        }
    }

    reader.endObject()

    if (id == null) {
        throw IOException("Invalid build json: \"id\" is absent")
    }

    if (name == null) {
        throw IOException("Invalid build json: \"number\" is absent")
    }

    if (parentId == null) {
        throw IOException("Invalid build json: \"buildTypeId\" is absent")
    }

    if (status == null) {
        throw IOException("Invalid build json: \"status\" is absent")
    }

    return Build(id!!, name!!, parentId!!, status!!)
}


