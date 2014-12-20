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

package com.tcity.android.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.Preference;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;

public class PreferenceActivity extends android.preference.PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(R.string.settings);
        }

        //noinspection deprecation
        addPreferencesFromResource(R.xml.preferences);

        Preferences preferences = new Preferences(this);

        //noinspection deprecation
        Preference urlPreference = findPreference(getString(R.string.url_pref_key));
        urlPreference.setSummary(preferences.getUrl());
        urlPreference.setSelectable(false);

        //noinspection deprecation
        Preference loginPreference = findPreference(getResources().getString(R.string.login_pref_key));
        loginPreference.setSummary(preferences.getLogin());
        loginPreference.setSelectable(false);
    }
}
