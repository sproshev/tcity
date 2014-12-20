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

package com.tcity.android.background.rest;

import com.tcity.android.app.Preferences;

import org.jetbrains.annotations.NotNull;

class RestLocator {

    @NotNull
    private static final String REST_PREFIX = "/httpAuth/app/rest/";

    private RestLocator() {
    }

    @NotNull
    public static String getProjectsUrl(@NotNull Preferences preferences) {
        return preferences.getUrl() + REST_PREFIX + "projects";
    }

    @NotNull
    public static String getBuildConfigurationsUrl(@NotNull String projectId,
                                                   @NotNull Preferences preferences) {
        return preferences.getUrl() + REST_PREFIX + "projects/id:" + projectId + "/buildTypes";
    }

    @NotNull
    public static String getBuildsUrl(@NotNull String buildConfigurationId,
                                      @NotNull Preferences preferences) {
        return preferences.getUrl() + REST_PREFIX + "builds/?locator=" +
                "buildType:(id:" + buildConfigurationId + ")," +
                "running:any," +
                "branch:(branched:any)";
    }

    @NotNull
    public static String getProjectStatusUrl(@NotNull String id, @NotNull Preferences preferences) {
        return preferences.getUrl() + REST_PREFIX + "projects/id:" + id + "/status";
    }

    @NotNull
    public static String getBuildConfigurationStatusUrl(@NotNull String id,
                                                        @NotNull Preferences preferences) {
        return preferences.getUrl() + REST_PREFIX + "buildTypes/id:" + id + "/status";
    }
}