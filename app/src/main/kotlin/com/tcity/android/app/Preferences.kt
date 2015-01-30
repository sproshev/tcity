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
import android.util.Base64
import com.tcity.android.R
import com.tcity.android.db.DBUtils
import com.tcity.android.sync.SyncUtils


public class Preferences(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val syncKey = context.getString(R.string.sync_pref_key)
    private val syncWifiOnlyKey = context.getString(R.string.sync_wifi_only_pref_key)
    private val syncIntervalKey = context.getString(R.string.sync_interval_pref_key)

    private val archivedBuildLogKey = context.getString(R.string.archived_build_log_pref_key)

    class object {
        private val URL_KEY = "url"
        private val LOGIN_KEY = "login"
        private val AUTH_KEY = "auth"
        private val SYNC_RECEIVER_KEY = "sync_receiver"
        private val SYNC_SCHEDULED_KEY = "sync_scheduled"
        private val FAVOURITE_PROJECTS_LAST_UPDATE_KEY = "favourite_projects_last_update"
        private val SERVER_VERSION_KEY = "server_version"
        private val SERVER_VERSION_LAST_UPDATE_KEY = "server_version_last_update"
    }

    public fun isValid(): Boolean {
        return preferences.contains(URL_KEY) && preferences.contains(LOGIN_KEY) && preferences.contains(AUTH_KEY)
    }

    public fun getUrl(): String = preferences.getString(URL_KEY, null)

    public fun getLogin(): String = preferences.getString(LOGIN_KEY, null)

    public fun getAuth(): String = preferences.getString(AUTH_KEY, null)

    public fun isSyncEnabled(): Boolean = preferences.getBoolean(syncKey, true)

    public fun isSyncWifiOnly(): Boolean = preferences.getBoolean(syncWifiOnlyKey, true)

    public fun isSyncReceiverEnabled(): Boolean = preferences.getBoolean(SYNC_RECEIVER_KEY, false)

    public fun isSyncScheduled(): Boolean = preferences.getBoolean(SYNC_SCHEDULED_KEY, false)

    public fun isBuildLogArchived(): Boolean = preferences.getBoolean(archivedBuildLogKey, true)

    public fun getFavouriteProjectsLastUpdate(): Long {
        return preferences.getLong(FAVOURITE_PROJECTS_LAST_UPDATE_KEY, DBUtils.UNDEFINED_TIME)
    }

    public fun getSyncInterval(): Int {
        return preferences.getInt(syncIntervalKey, SyncUtils.DEFAULT_INTERVAL)
    }

    public fun getServerVersion(): String? = preferences.getString(SERVER_VERSION_KEY, null)

    public fun getServerVersionLastUpdate(): Long {
        return preferences.getLong(SERVER_VERSION_LAST_UPDATE_KEY, DBUtils.UNDEFINED_TIME)
    }

    public fun setUrl(url: String) {
        val editor = preferences.edit()

        editor.putString(URL_KEY, url)

        editor.apply()
    }

    public fun setAuth(login: String, password: String) {
        val editor = preferences.edit()

        editor.putString(LOGIN_KEY, login)

        editor.putString(AUTH_KEY, Base64.encodeToString("$login:$password".toByteArray(), Base64.NO_WRAP))

        editor.apply()
    }

    public fun setSyncEnabled(enabled: Boolean) {
        val editor = preferences.edit()

        editor.putBoolean(syncKey, enabled)

        editor.apply()
    }

    public fun setSyncWifiOnly(wifiOnly: Boolean) {
        val editor = preferences.edit()

        editor.putBoolean(syncWifiOnlyKey, wifiOnly)

        editor.apply()
    }

    public fun setSyncReceiverEnabled(enabled: Boolean) {
        val editor = preferences.edit()

        editor.putBoolean(SYNC_RECEIVER_KEY, enabled)

        editor.apply()
    }

    public fun setSyncScheduled(scheduled: Boolean) {
        val editor = preferences.edit()

        editor.putBoolean(SYNC_SCHEDULED_KEY, scheduled)

        editor.apply()
    }

    public fun setFavouriteProjectsLastUpdate(time: Long) {
        val editor = preferences.edit()

        editor.putLong(FAVOURITE_PROJECTS_LAST_UPDATE_KEY, time)

        editor.apply()
    }

    public fun setSyncInterval(interval: Int) {
        val editor = preferences.edit()

        if (interval < SyncUtils.MIN_INTERVAL) {
            editor.putInt(syncIntervalKey, SyncUtils.MIN_INTERVAL)
        } else if (interval > SyncUtils.MAX_INTERVAL) {
            editor.putInt(syncIntervalKey, SyncUtils.MAX_INTERVAL)
        } else {
            editor.putInt(syncIntervalKey, interval)
        }

        editor.apply()
    }

    public fun setServerVersion(version: String) {
        val editor = preferences.edit();

        editor.putString(SERVER_VERSION_KEY, version)
        editor.putLong(SERVER_VERSION_LAST_UPDATE_KEY, System.currentTimeMillis())

        editor.apply()
    }

    public fun reset() {
        preferences.edit().clear().apply()
    }
}
