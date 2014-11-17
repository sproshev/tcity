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

import java.util.HashSet;
import java.util.Set;

public class Settings {

    @NotNull
    private static final String WATCHED_PROJECTS_KEY = "watch_projects";

    @Nullable
    private static Settings INSTANCE = null;

    @NotNull
    private final SharedPreferences myPreferences;

    private Settings(@NotNull Context context) {
        myPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static Settings getInstance(@NotNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Settings(context);
        }

        return INSTANCE;
    }

    public void watchProject(@NotNull String id) {
        Set<String> watchedProjects = myPreferences.getStringSet(WATCHED_PROJECTS_KEY, new HashSet<String>());

        if (!watchedProjects.contains(id)) {
            watchedProjects.add(id);

            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putStringSet(WATCHED_PROJECTS_KEY, watchedProjects);
            editor.apply();
        }
    }

    public void unwatchProject(@NotNull String id) {
        Set<String> watchedProjects = myPreferences.getStringSet(WATCHED_PROJECTS_KEY, new HashSet<String>());

        if (watchedProjects.contains(id)) {
            watchedProjects.remove(id);

            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putStringSet(WATCHED_PROJECTS_KEY, watchedProjects);
            editor.apply();
        }
    }

    public boolean isWatchedProject(@NotNull String id) {
        return myPreferences.getStringSet(WATCHED_PROJECTS_KEY, new HashSet<String>()).contains(id);
    }
}
