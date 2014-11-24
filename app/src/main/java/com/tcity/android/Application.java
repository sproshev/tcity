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

package com.tcity.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Application extends android.app.Application {

    @Nullable
    private Preferences myPreferences;

    @NotNull
    public Preferences getPreferences() {
        if (myPreferences == null) {
            myPreferences = new Preferences(this);
        }

        return myPreferences;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (myPreferences != null) {
            myPreferences.onTrimMemory();
        }
    }

    public static class Preferences {

        @NotNull
        private static final String WATCHED_PROJECT_IDS_KEY = "watch_projects";

        @NotNull
        private final SharedPreferences myPreferences;

        @Nullable
        private Set<String> myWatchedProjectIds = null;

        private Preferences(@NotNull Context context) {
            myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @NotNull
        public Set<String> getWatchedProjectIds() {
            if (myWatchedProjectIds == null) {
                loadWatchedProjectIds();
            }

            return Collections.unmodifiableSet(myWatchedProjectIds);
        }

        public void addWatchedProjectId(@NotNull String id) {
            if (myWatchedProjectIds == null) {
                loadWatchedProjectIds();
            }

            myWatchedProjectIds.add(id);

            saveSet(WATCHED_PROJECT_IDS_KEY, myWatchedProjectIds);
        }

        public void removeWatchedProjectId(@NotNull String id) {
            if (myWatchedProjectIds == null) {
                loadWatchedProjectIds();
            }

            myWatchedProjectIds.remove(id);

            saveSet(WATCHED_PROJECT_IDS_KEY, myWatchedProjectIds);
        }

        private void loadWatchedProjectIds() {
            myWatchedProjectIds = new HashSet<>(
                    myPreferences.getStringSet(
                            WATCHED_PROJECT_IDS_KEY,
                            Collections.<String>emptySet()
                    )
            );
        }

        private void saveSet(@NotNull String key, @NotNull Set<String> values) {
            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putStringSet(key, values);
            editor.apply();
        }

        private void onTrimMemory() {
            myWatchedProjectIds = null;
        }
    }
}
