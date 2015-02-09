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
import com.tcity.android.obj.QueuedBuild
import com.tcity.android.db.DB

throws(javaClass<IOException>())
public fun parseQueuedBuilds(stream: InputStream, db: DB): List<QueuedBuild> {
    return parse(
            stream,
            "build",
            ::parseQueuedBuild,
            { db.isBuildConfigurationFavourite(it.parentBuildConfigurationId) }
    )
}

throws(javaClass<IOException>())
private fun parseQueuedBuild(reader: JsonReader): QueuedBuild {
    reader.beginObject()

    var id: String? = null
    var parentId: String? = null
    var branch: String? = null
    var isBranchDefault = false

    while (reader.hasNext()) {
        when (reader.nextName()) {
            "id" -> id = reader.nextString()
            "buildTypeId" -> parentId = reader.nextString()
            "branchName" -> branch = reader.nextString()
            "defaultBranch" -> isBranchDefault = reader.nextBoolean()
            else -> reader.skipValue()
        }
    }

    reader.endObject()

    if (id == null) {
        throw IOException("Invalid running build json: \"id\" is absent")
    }

    if (parentId == null) {
        throw IOException("Invalid running build json: \"buildTypeId\" is absent")
    }

    return QueuedBuild(id!!, parentId!!, branch, isBranchDefault)
}
