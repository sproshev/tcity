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


public class Preferences(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val urlKey = context.getString(R.string.url_pref_key)
    private val loginKey = context.getString(R.string.login_pref_key)

    private val syncKey = context.getString(R.string.sync_pref_key)
    private val syncWifiOnlyKey = context.getString(R.string.sync_wifi_only_pref_key)

    class object {
        private val AUTH_KEY = "auth"
    }

    public fun isValid(): Boolean {
        return preferences.contains(urlKey) && preferences.contains(loginKey) && preferences.contains(AUTH_KEY)
    }

    public fun getUrl(): String = preferences.getString(urlKey, null)

    public fun getLogin(): String = preferences.getString(loginKey, null)

    public fun getAuth(): String = preferences.getString(AUTH_KEY, null)

    public fun isSyncEnabled(): Boolean = preferences.getBoolean(syncKey, true)

    public fun isSyncWifiOnly(): Boolean = preferences.getBoolean(syncWifiOnlyKey, true)

    public fun setUrl(url: String) {
        val editor = preferences.edit()

        editor.putString(urlKey, url)

        editor.apply()
    }

    public fun setAuth(login: String, password: String) {
        val editor = preferences.edit()

        editor.putString(loginKey, login)

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
}
