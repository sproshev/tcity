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

import android.content.Context
import android.preference.PreferenceManager


public class Preferences protected (context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val watchedProjectIds = WatchedConceptIdsPreference("watched_projects", preferences)
    private val watchedBuildConfigurationIds = WatchedConceptIdsPreference("watched_build_configurations", preferences)
    private val watchedBuildIds = WatchedConceptIdsPreference("watched_builds", preferences)

    private val loginPreference = LoginPreference(preferences)

    public fun getUrl(): String = loginPreference.getUrl()
    public fun getAuth(): String = loginPreference.getAuth()

    public fun getWatchedProjectIds(): Set<String> = watchedProjectIds.get()
    public fun getWatchedBuildConfigurationIds(): Set<String> = watchedBuildConfigurationIds.get()
    public fun getWatchedBuildIds(): Set<String> = watchedBuildIds.get()

    public fun addWatchedProjectId(id: String): Unit = watchedProjectIds.add(id)
    public fun addWatchedBuildConfigurationId(id: String): Unit = watchedBuildConfigurationIds.add(id)
    public fun addWatchedBuildId(id: String): Unit = watchedBuildIds.add(id)

    public fun removeWatchedProjectId(id: String): Unit = watchedProjectIds.remove(id)
    public fun removeWatchedBuildConfigurationId(id: String): Unit = watchedBuildConfigurationIds.remove(id)
    public fun removeWatchedBuildId(id: String): Unit = watchedBuildIds.remove(id)

    protected fun onTrimMemory() {
        watchedProjectIds.onTrimMemory()
        watchedBuildConfigurationIds.onTrimMemory()
        watchedBuildIds.onTrimMemory()
    }
}
