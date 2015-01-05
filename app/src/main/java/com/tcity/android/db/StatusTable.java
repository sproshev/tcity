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

enum StatusTable {
    PROJECT, BUILD_CONFIGURATION;

    @NotNull
    String getName() {
        switch (this) {
            case PROJECT:
                return "project_status";
            case BUILD_CONFIGURATION:
                return "build_configuration_status";
            default:
                throw new IllegalArgumentException();
        }
    }
}