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

package com.tcity.android.rest

import com.tcity.android.app.Preferences

private val REST_PATH = "/httpAuth/app/rest/"

public fun getProjectsUrl(preferences: Preferences): String = "${preferences.getUrl()}${REST_PATH}projects"

public fun getBuildConfigurationsUrl(projectId: String, preferences: Preferences): String {
    return "${preferences.getUrl()}${REST_PATH}projects/id:$projectId/buildTypes"
}

public fun getBuildsUrl(buildConfigurationId: String, preferences: Preferences): String {
    return "${preferences.getUrl()}${REST_PATH}builds/?locator=buildType:(id:$buildConfigurationId),running:any,branch:(branched:any)"
}

public fun getProjectStatusUrl(id: String, preferences: Preferences): String {
    return "${preferences.getUrl()}${REST_PATH}projects/id:$id/status"
}

public fun getBuildConfigurationStatusUrl(id: String, preferences: Preferences): String {
    return "${preferences.getUrl()}${REST_PATH}buildTypes/id:$id/status"
}

public fun getProjectWebUrl(id: String, preferences: Preferences): String {
    return "${preferences.getUrl()}/project.html?projectId=$id"
}