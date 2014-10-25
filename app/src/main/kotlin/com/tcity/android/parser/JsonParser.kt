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

import com.tcity.android.model.Project
import java.io.IOException
import java.io.InputStream
import android.util.JsonReader
import java.io.InputStreamReader
import java.util.ArrayList

[throws(javaClass<IOException>())]
public fun parseProjects(stream: InputStream): List<Project> {
    val reader = JsonReader(InputStreamReader(stream))

    var result: List<Project>? = null
    var capacity = 10

    try {
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "count" -> capacity = reader.nextInt()
                "project" -> result = readProjects(reader, capacity)
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
        throw IOException("Invalid projects json: \"projects\" is absent.")
    }
}

[throws(javaClass<IOException>())]
private fun readProjects(reader: JsonReader, capacity: Int): List<Project> {
    val result = ArrayList<Project>(Math.max(capacity, 0));

    reader.beginArray()

    while (reader.hasNext()) {
        result.add(readProject(reader))
    }

    reader.endArray()

    return result
}

[throws(javaClass<IOException>())]
private fun readProject(reader: JsonReader): Project {
    reader.beginObject()

    var id: String? = null
    var name: String? = null
    var parentId: String? = null

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "id" -> id = reader.nextString()
            "name" -> name = reader.nextString()
            "parentProjectId" -> parentId = reader.nextString()
            else -> reader.skipValue()
        }
    }

    reader.endObject()

    if (id == null) {
        throw IOException("Invalid project json: \"id\" is absent.")
    }

    if (name == null) {
        throw IOException("Invalid project json: \"name\" is absent.")
    }

    if (parentId == null) {
        throw IOException("Invalid project json: \"parentId\" is absent.")
    }

    return Project(id!!, name!!, parentId!!)
}