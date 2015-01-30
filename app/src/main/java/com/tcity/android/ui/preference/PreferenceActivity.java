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

package com.tcity.android.ui.preference;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.view.MenuItem;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;
import com.tcity.android.sync.SyncUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreferenceActivity extends android.preference.PreferenceActivity {

    @NotNull
    private Preferences myPreferences;

    @NotNull
    private Preference myIntervalPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(R.string.settings);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        //noinspection deprecation
        addPreferencesFromResource(R.xml.preferences);

        myPreferences = new Preferences(this);

        //noinspection deprecation
        Preference serverPreference = findPreference(getString(R.string.server_pref_key));
        serverPreference.setTitle(myPreferences.getUrl());
        serverPreference.setSummary(myPreferences.getServerVersion());
        serverPreference.setOnPreferenceClickListener(new ServerListener());

        //noinspection deprecation
        Preference logoutPreference = findPreference(getString(R.string.logout_pref_key));
        logoutPreference.setSummary(myPreferences.getLogin());
        logoutPreference.setOnPreferenceClickListener(new LogoutPreferenceListener());

        //noinspection deprecation
        CheckBoxPreference syncPreference = (CheckBoxPreference) findPreference(getString(R.string.sync_pref_key));
        syncPreference.setOnPreferenceChangeListener(new SyncPreferenceListener());

        //noinspection deprecation
        CheckBoxPreference wifiPreference = (CheckBoxPreference) findPreference(getString(R.string.sync_wifi_only_pref_key));
        wifiPreference.setOnPreferenceChangeListener(new WifiPreferenceListener());

        //noinspection deprecation
        myIntervalPreference = findPreference(getString(R.string.sync_interval_pref_key));
        myIntervalPreference.setOnPreferenceClickListener(new IntervalPreferenceListener());

        //noinspection deprecation
        Preference gitHubPreference = findPreference(getString(R.string.github_pref_key));
        gitHubPreference.setOnPreferenceClickListener(new GitHubPreferenceListener());

        //noinspection deprecation
        Preference googlePlayPreference = findPreference(getString(R.string.google_play_pref_key));
        googlePlayPreference.setOnPreferenceClickListener(new GooglePlayPreferenceListener());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            myIntervalPreference.setSummary(
                    "Sync will be running every " + myPreferences.getSyncInterval() + " minutes"
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ServerListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(myPreferences.getUrl()));

            startActivity(intent);

            return true;
        }
    }

    private class LogoutPreferenceListener implements Preference.OnPreferenceClickListener {

        @NotNull
        private static final String DIALOG_TAG = "LOGOUT";

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            LogoutDialogFragment dialog = new LogoutDialogFragment();

            dialog.show(getFragmentManager(), DIALOG_TAG);

            return true;
        }
    }

    private class SyncPreferenceListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(@Nullable Preference preference,
                                          @NotNull Object newValue) {
            PreferenceActivity context = PreferenceActivity.this;

            if ((boolean) newValue) {
                SyncUtils.enableSync(context, myPreferences.getSyncInterval(), myPreferences.isSyncWifiOnly());
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
            SyncUtils.updateSyncConnection(
                    PreferenceActivity.this,
                    myPreferences.getSyncInterval(),
                    (boolean) newValue
            );

            return true;
        }
    }

    private class IntervalPreferenceListener implements Preference.OnPreferenceClickListener {

        @NotNull
        private static final String DIALOG_TAG = "INTERVAL";

        @Override
        public boolean onPreferenceClick(Preference preference) {
            SyncIntervalDialogFragment fragment = new SyncIntervalDialogFragment();

            fragment.show(getFragmentManager(), DIALOG_TAG);

            return true;
        }
    }

    private class GitHubPreferenceListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/sproshev/tcity"));

            startActivity(intent);

            return true;
        }
    }

    private class GooglePlayPreferenceListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));

            startActivity(intent);

            return true;
        }
    }
}
