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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;
import com.tcity.android.sync.SyncUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreferenceFragment extends android.preference.PreferenceFragment {

    @NotNull
    public static final String TAG = PreferenceFragment.class.getName();

    @NotNull
    private Preferences myPreferences;

    @NotNull
    private Preference mySyncInterval;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        myPreferences = new Preferences(getActivity());

        Preference server = findPreference(getString(R.string.server_pref_key));
        server.setTitle(myPreferences.getUrl());
        server.setSummary(myPreferences.getServerVersion());
        server.setOnPreferenceClickListener(new ServerListener());

        Preference logout = findPreference(getString(R.string.logout_pref_key));
        logout.setSummary(myPreferences.getLogin());
        logout.setOnPreferenceClickListener(new LogoutListener());

        CheckBoxPreference sync = (CheckBoxPreference) findPreference(getString(R.string.sync_pref_key));
        sync.setOnPreferenceChangeListener(new SyncListener());

        CheckBoxPreference syncWifiOnly = (CheckBoxPreference) findPreference(getString(R.string.sync_wifi_only_pref_key));
        syncWifiOnly.setOnPreferenceChangeListener(new SyncWifiOnlyListener());

        mySyncInterval = findPreference(getString(R.string.sync_interval_pref_key));
        mySyncInterval.setOnPreferenceClickListener(new SyncIntervalListener());

        Preference gitHub = findPreference(getString(R.string.github_pref_key));
        gitHub.setOnPreferenceClickListener(new GitHubListener());

        Preference googlePlay = findPreference(getString(R.string.google_play_pref_key));
        googlePlay.setOnPreferenceClickListener(new GooglePlayListener());

        reloadSyncInterval();
    }

    void reloadSyncInterval() {
        mySyncInterval.setSummary(
                "Sync will be running every " + myPreferences.getSyncInterval() + " minutes"
        );
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

    private class LogoutListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            LogoutDialogFragment dialog = new LogoutDialogFragment();

            dialog.show(getFragmentManager(), LogoutDialogFragment.TAG);

            return true;
        }
    }

    private class SyncListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(@Nullable Preference preference,
                                          @NotNull Object newValue) {
            Activity context = getActivity();

            if ((boolean) newValue) {
                SyncUtils.enableSync(
                        context,
                        myPreferences.getSyncInterval(),
                        myPreferences.isSyncWifiOnly()
                );
            } else {
                SyncUtils.disableSync(context);
            }

            return true;
        }
    }

    private class SyncWifiOnlyListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(@Nullable Preference preference,
                                          @NotNull Object newValue) {
            SyncUtils.updateSyncConnection(
                    getActivity(),
                    myPreferences.getSyncInterval(),
                    (boolean) newValue
            );

            return true;
        }
    }

    private class SyncIntervalListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            SyncIntervalDialogFragment fragment = new SyncIntervalDialogFragment();

            fragment.setTargetFragment(PreferenceFragment.this, 0);
            fragment.show(getFragmentManager(), SyncIntervalDialogFragment.TAG);

            return true;
        }
    }

    private class GitHubListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/sproshev/tcity"));

            startActivity(intent);

            return true;
        }
    }

    private class GooglePlayListener implements Preference.OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(@Nullable Preference preference) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getActivity().getPackageName()));

            startActivity(intent);

            return true;
        }
    }
}
