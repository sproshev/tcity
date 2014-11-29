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

        @NotNull
        private final Set<String> myWatchedProjectIds = new HashSet<>();

        private boolean myWatchedProjectIdsLoaded = false;

        private Preferences(@NotNull Context context) {
            myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @NotNull
        public Set<String> getWatchedProjectIds() {
            ensureWatchedProjectIdsLoaded();

            return Collections.unmodifiableSet(myWatchedProjectIds);
        }

        public void addWatchedProjectId(@NotNull String id) {
            ensureWatchedProjectIdsLoaded();

            myWatchedProjectIds.add(id);

            saveSet(WATCHED_PROJECT_IDS_KEY, myWatchedProjectIds);
        }

        public void removeWatchedProjectId(@NotNull String id) {
            ensureWatchedProjectIdsLoaded();

            myWatchedProjectIds.remove(id);

            saveSet(WATCHED_PROJECT_IDS_KEY, myWatchedProjectIds);
        }

        private void ensureWatchedProjectIdsLoaded() {
            if (!myWatchedProjectIdsLoaded) {
                updateSet(WATCHED_PROJECT_IDS_KEY, myWatchedProjectIds);
                myWatchedProjectIdsLoaded = true;
            }
        }

        private void updateSet(@NotNull String key, @NotNull Set<String> set) {
            set.clear();

            set.addAll(
                    myPreferences.getStringSet(
                            key,
                            Collections.<String>emptySet()
                    )
            );
        }

        private void saveSet(@NotNull String key, @NotNull Set<String> set) {
            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putStringSet(key, set);
            editor.apply();
        }

        private void onTrimMemory() {
            myWatchedProjectIdsLoaded = false;
            myWatchedProjectIds.clear();
        }
    }
}
