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

import android.app.AlarmManager;

import com.tcity.android.app.Preferences;
import com.tcity.android.db.DB;
import com.tcity.android.db.Project;

import org.jetbrains.annotations.NotNull;

public class ExpirationUtils {

    private ExpirationUtils() {
    }

    public static boolean isServerVersionExpired(@NotNull Preferences preferences) {
        return preferences.getServerVersionLastUpdate() <
                System.currentTimeMillis() - AlarmManager.INTERVAL_DAY * 7;
    }

    public static boolean areProjectsExpired(@NotNull DB db) {
        return isEarlierThan3DaysAgo(db.getProjectLastUpdate(Project.ROOT_PROJECT_ID));
    }

    public static boolean areBuildConfigurationsExpired(@NotNull DB db, @NotNull String projectId) {
        return isEarlierThan3DaysAgo(db.getProjectLastUpdate(projectId));
    }

    public static boolean areFavouriteProjectsExpired(@NotNull Preferences preferences) {
        return isEarlierThan3DaysAgo(preferences.getFavouriteProjectsLastUpdate());
    }

    public static boolean areBuildsExpired(@NotNull DB db, @NotNull String buildConfigurationId) {
        return isEarlierThan10MinutesAgo(db.getBuildConfigurationLastUpdate(buildConfigurationId));
    }

    public static boolean isProjectStatusExpired(@NotNull DB db, @NotNull String id) {
        return isEarlierThan10MinutesAgo(db.getProjectStatusLastUpdate(id));
    }

    public static boolean isBuildConfigurationStatusExpired(@NotNull DB db, @NotNull String id) {
        return isEarlierThan10MinutesAgo(db.getBuildConfigurationStatusLastUpdate(id));
    }

    private static boolean isEarlierThan3DaysAgo(long lastUpdate) {
        return lastUpdate < System.currentTimeMillis() - AlarmManager.INTERVAL_DAY * 3;
    }

    private static boolean isEarlierThan10MinutesAgo(long lastUpdate) {
        return lastUpdate < System.currentTimeMillis() - 2 * AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3;
    }
}
