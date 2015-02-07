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

import com.tcity.android.obj.BuildInfo
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import android.util.JsonReader
import com.tcity.android.app.Common
import java.text.SimpleDateFormat

throws(javaClass<IOException>())
public fun parseBuildInfo(stream: InputStream): BuildInfo {
    val reader = JsonReader(InputStreamReader(stream))

    try {
        reader.beginObject()

        val result = BuildInfo()
        val dateFormat = SimpleDateFormat(Common.TEAMCITY_DATE_FORMAT)

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "status" -> if (result.status == null) {
                    result.status = com.tcity.android.Status.valueOf(reader.nextString())
                }
                "running" -> if (reader.nextBoolean()) {
                    result.status = com.tcity.android.Status.RUNNING
                }
                "branchName" -> result.branch = reader.nextString()
                "defaultBranch" -> result.isBranchDefault = reader.nextBoolean()
                "statusText" -> result.result = reader.nextString()
                "waitReason" -> result.waitReason = reader.nextString()
                "queuedDate" -> result.queued = dateFormat.parse(reader.nextString())
                "startDate" -> result.started = dateFormat.parse(reader.nextString())
                "finishDate" -> result.finished = dateFormat.parse(reader.nextString())
                "agent" -> result.agent = getAgentName(reader)
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return result
    } finally {
        reader.close()
    }
}

throws(javaClass<IOException>())
private fun getAgentName(reader: JsonReader): String? {
    reader.beginObject()

    var result: String? = null

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "name" -> result = reader.nextString()
            else -> reader.skipValue()
        }
    }

    reader.endObject()

    return result
}
