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
import java.io.InputStreamReader
import java.io.IOException
import java.util.ArrayList
import java.util.Collections

throws(javaClass<IOException>())
private fun <T> parse(
        stream: InputStream,
        key: String,
        parser: (JsonReader) -> T,
        filter: (T) -> Boolean = { true }
): List<T> {
    val reader = JsonReader(InputStreamReader(stream))

    var result: MutableList<T>? = null
    var capacity = 10

    try {
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "count" -> capacity = reader.nextInt()

                key -> {
                    result = ArrayList<T>(Math.max(capacity, 0))

                    reader.beginArray()

                    while (reader.hasNext()) {
                        val value = parser(reader)

                        if (filter(value)) {
                            result!!.add(value)
                        }
                    }

                    reader.endArray()
                }

                else -> reader.skipValue()
            }
        }

        reader.endObject()
    } finally {
        reader.close()
    }

    if (result != null) {
        return result!!
    } else if (capacity == 0) {
        return Collections.emptyList()
    } else {
        throw IOException("Invalid json: \"$key\" is absent")
    }
}
