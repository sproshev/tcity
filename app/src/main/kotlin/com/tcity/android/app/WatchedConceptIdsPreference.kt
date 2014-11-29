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

import android.content.SharedPreferences
import java.util.HashSet
import java.util.Collections

private class WatchedConceptIdsPreference(
        private val key: String,
        private val preferences: SharedPreferences
) {

    private val ids = HashSet<String>()
    private var idsLoaded = false

    public fun get(): Set<String> {
        ensureLoaded()

        return Collections.unmodifiableSet<String>(ids)
    }

    public fun add(id: String) {
        ensureLoaded()

        ids.add(id)

        save(key, ids)
    }

    public fun remove(id: String) {
        ensureLoaded()

        ids.remove(id)

        save(key, ids)
    }

    public fun onTrimMemory() {
        idsLoaded = false

        ids.clear()
    }

    private fun ensureLoaded() {
        if (!idsLoaded) {
            reload(key, ids)

            idsLoaded = true
        }
    }

    private fun reload(key: String, set: MutableSet<String>) {
        set.clear()

        set.addAll(preferences.getStringSet(key, Collections.emptySet()))
    }

    private fun save(key: String, set: Set<String>) {
        val editor = preferences.edit()

        editor.putStringSet(key, set)

        editor.apply()
    }
}
