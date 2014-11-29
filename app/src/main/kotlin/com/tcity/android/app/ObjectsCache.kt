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

package com.tcity.android.app

import com.tcity.android.db.DBHelper
import com.tcity.android.parser.BuildConfigurationsParser
import com.tcity.android.parser.ProjectsParser
import android.content.Context
import java.lang.ref.WeakReference

private class ObjectsCache(private val context: Context) {

    private var dbHelperReference: WeakReference<DBHelper>? = null

    private var buildConfigurationsParserReference: WeakReference<BuildConfigurationsParser>? = null
    private var projectsParserReference: WeakReference<ProjectsParser>? = null

    public fun getDBHelper(): DBHelper {
        val result = dbHelperReference?.get()

        if (result == null) {
            val newInstance = DBHelper(context)

            dbHelperReference = WeakReference(newInstance)

            return newInstance
        } else {
            return result
        }
    }

    public fun getBuildConfigurationsParser(): BuildConfigurationsParser {
        val result = buildConfigurationsParserReference?.get()

        if (result == null) {
            val newInstance = BuildConfigurationsParser()

            buildConfigurationsParserReference = WeakReference(newInstance)

            return newInstance
        } else {
            return result
        }
    }

    public fun getProjectsParser(): ProjectsParser {
        val result = projectsParserReference?.get()

        if (result == null) {
            val newInstance = ProjectsParser()

            projectsParserReference = WeakReference(newInstance)

            return newInstance
        } else {
            return result
        }
    }
}
