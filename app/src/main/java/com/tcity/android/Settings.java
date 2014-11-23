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
import java.util.Set;

public class Settings {

    @NotNull
    private static final String WATCHED_PROJECT_IDS_KEY = "watch_projects";

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

    public void setWatchedProjectIds(@NotNull Set<String> ids) {
        SharedPreferences.Editor editor = myPreferences.edit();
        editor.putStringSet(WATCHED_PROJECT_IDS_KEY, ids);
        editor.apply();
    }

    @NotNull
    public Set<String> getWatchedProjectIds() {
        return myPreferences.getStringSet(WATCHED_PROJECT_IDS_KEY, Collections.<String>emptySet());
    }
}
