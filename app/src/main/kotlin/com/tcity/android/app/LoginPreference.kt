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
import android.util.Base64

private class LoginPreference(private val preferences: SharedPreferences) {

    class object {
        private val URL_KEY = "url"
        private val LOGIN_KEY = "login"
        private val AUTH_KEY = "auth"
    }

    public fun exists(): Boolean {
        return preferences.contains(URL_KEY) && preferences.contains(LOGIN_KEY) && preferences.contains(AUTH_KEY)
    }

    public fun getUrl(): String = preferences.getString(URL_KEY, null)

    public fun getLogin(): String = preferences.getString(LOGIN_KEY, null)

    public fun getAuth(): String = preferences.getString(AUTH_KEY, null)

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
}


