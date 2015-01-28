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

package com.tcity.android.db;

import org.jetbrains.annotations.NotNull;

class Table {

    @NotNull
    static final String PROJECT_STATUS_TABLE = "project_status";

    @NotNull
    static final String BUILD_CONFIGURATION_STATUS_TABLE = "build_configuration_status";

    @NotNull
    static final String FAVOURITE_PROJECT_TABLE = "favourite_project";

    @NotNull
    static final String FAVOURITE_BUILD_CONFIGURATION_TABLE = "favourite_build_configuration";

    @NotNull
    static final String FAVOURITE_BUILD_TABLE = "favourite_build";

    @NotNull
    static final String PROJECT_OVERVIEW_TABLE = "project_overview";

    @NotNull
    static final String BUILD_CONFIGURATION_OVERVIEW_TABLE = "build_configuration_overview";

    @NotNull
    static final String BUILD_OVERVIEW_TABLE = "build_overview";

    @NotNull
    static final String PROJECT_TIME_TABLE = "project_time";

    @NotNull
    static final String BUILD_CONFIGURATION_TIME_TABLE = "build_configuration_time";

    private Table() {
    }
}
