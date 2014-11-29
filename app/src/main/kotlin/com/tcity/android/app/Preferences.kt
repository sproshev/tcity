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

    private val watchedProjectIdsPreference = WatchedConceptIdsPreference("watched_projects", preferences)
    private val watchedBuildConfigurationIdsPreference = WatchedConceptIdsPreference("watched_build_configurations", preferences)
    private val watchedBuildIdsPreference = WatchedConceptIdsPreference("watched_builds", preferences)

    private val loginPreference = LoginPreference(preferences)

    public fun getUrl(): String = loginPreference.getUrl()
    public fun getAuth(): String = loginPreference.getAuth()

    public fun getWatchedProjectIds(): Set<String> = watchedProjectIdsPreference.get()
    public fun getWatchedBuildConfigurationIds(): Set<String> = watchedBuildConfigurationIdsPreference.get()
    public fun getWatchedBuildIds(): Set<String> = watchedBuildIdsPreference.get()

    public fun addWatchedProjectIds(id: String): Unit = watchedProjectIdsPreference.add(id)
    public fun addWatchedBuildConfigurationIds(id: String): Unit = watchedBuildConfigurationIdsPreference.add(id)
    public fun addWatchedBuildIds(id: String): Unit = watchedBuildIdsPreference.add(id)

    public fun removeWatchedProjectIds(id: String): Unit = watchedProjectIdsPreference.remove(id)
    public fun removeWatchedBuildConfigurationIds(id: String): Unit = watchedBuildConfigurationIdsPreference.remove(id)
    public fun removeWatchedBuildIds(id: String): Unit = watchedBuildIdsPreference.remove(id)

    protected fun onTrimMemory() {
        watchedProjectIdsPreference.onTrimMemory()
        watchedBuildConfigurationIdsPreference.onTrimMemory()
        watchedBuildIdsPreference.onTrimMemory()
    }
}
