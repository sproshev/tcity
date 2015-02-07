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

import java.io.IOException
import java.io.InputStream
import com.tcity.android.obj.BuildArtifact
import android.util.JsonReader

throws(javaClass<IOException>())
public fun parseBuildArtifacts(stream: InputStream): List<BuildArtifact> {
    return parse(stream, "file", ::parseBuildArtifact)
}

throws(javaClass<IOException>())
private fun parseBuildArtifact(reader: JsonReader): BuildArtifact {
    reader.beginObject()

    var size: Long = -1
    var name: String? = null
    var contentHref: String? = null
    var childrenHref: String? = null

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "size" -> size = reader.nextLong()
            "name" -> name = reader.nextString()
            "children" -> childrenHref = getHref(reader)
            "content" -> contentHref = getHref(reader)
            else -> reader.skipValue()
        }
    }

    reader.endObject()

    if (name == null) {
        throw IllegalStateException("Invalid artifacts json: \"name\" is absent")
    }

    if (contentHref == null && childrenHref == null) {
        throw IllegalStateException("Invalid artifacts json: \"content\" and \"children\" are absent")
    }

    return BuildArtifact(size, name, contentHref, childrenHref)
}


throws(javaClass<IOException>())
private fun getHref(reader: JsonReader): String? {
    reader.beginObject()

    var result: String? = null

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "href" -> result = reader.nextString()
            else -> reader.skipValue()
        }
    }

    reader.endObject()

    return result
}
