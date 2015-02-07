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
import android.util.JsonReader
import com.tcity.android.obj.RunningBuild

throws(javaClass<IOException>())
public fun parseRunningBuilds(stream: InputStream): List<RunningBuild> {
    return parse(stream, "build", ::parseRunningBuild)
}

throws(javaClass<IOException>())
private fun parseRunningBuild(reader: JsonReader): RunningBuild {
    reader.beginObject()

    var id: String? = null
    var name: String? = null
    var parentId: String? = null
    var percentageComplete = -1
    var branch: String? = null
    var isBranchDefault = false

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "id" -> id = reader.nextString()
            "number" -> name = reader.nextString()
            "buildTypeId" -> parentId = reader.nextString()
            "percentageComplete" -> percentageComplete = reader.nextInt()
            "branchName" -> branch = reader.nextString()
            "defaultBranch" -> isBranchDefault = reader.nextBoolean()
            else -> reader.skipValue()
        }
    }

    reader.endObject()

    if (id == null) {
        throw IOException("Invalid running build json: \"id\" is absent")
    }

    if (name == null) {
        throw IOException("Invalid running build json: \"number\" is absent")
    }

    if (parentId == null) {
        throw IOException("Invalid running build json: \"buildTypeId\" is absent")
    }

    if (percentageComplete == -1) {
        throw IOException("Invalid running build json: \"percentageComplete\" is absent")
    }

    return RunningBuild(id!!, name!!, parentId!!, percentageComplete, branch, isBranchDefault)
}
