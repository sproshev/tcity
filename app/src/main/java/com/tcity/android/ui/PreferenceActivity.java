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
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.Preferences;
import com.tcity.android.sync.SyncUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        //noinspection deprecation
        CheckBoxPreference syncPreference = (CheckBoxPreference) findPreference(getString(R.string.sync_pref_key));
        syncPreference.setOnPreferenceChangeListener(new SyncPreferenceListener());

        //noinspection deprecation
        CheckBoxPreference wifiPreference = (CheckBoxPreference) findPreference(getString(R.string.sync_wifi_only_pref_key));
        wifiPreference.setOnPreferenceChangeListener(new WifiPreferenceListener());

        //noinspection deprecation
        Preference logoutPreference = findPreference(getString(R.string.logout_pref_key));
        logoutPreference.setOnPreferenceClickListener(new LogoutPreferenceListener());
    }

    private class SyncPreferenceListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(@Nullable Preference preference,
                                          @NotNull Object newValue) {
            PreferenceActivity context = PreferenceActivity.this;

            if ((boolean) newValue) {
                SyncUtils.enableSync(context, new Preferences(context).isSyncWifiOnly());
            } else {
                SyncUtils.disableSync(context);
            }

            return true;
        }
    }

    private class WifiPreferenceListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(@Nullable Preference preference,
                                          @NotNull Object newValue) {
            SyncUtils.updateSync(PreferenceActivity.this, (boolean) newValue);

            return true;
        }
    }

    private class LogoutPreferenceListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            ((Application) getApplication()).getDB().reset();
            new Preferences(PreferenceActivity.this).reset();

            Intent intent = new Intent(PreferenceActivity.this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return true;
        }
    }
}
