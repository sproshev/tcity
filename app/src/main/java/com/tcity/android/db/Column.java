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

enum Column {
    TC_ID, NAME, PARENT_ID, STATUS, FAVOURITE, ANDROID_ID, BRANCH, TIME;

    @NotNull
    String getName() {
        switch (this) {
            case TC_ID:
                return "tc_id";
            case NAME:
                return "name";
            case PARENT_ID:
                return "parent_id";
            case STATUS:
                return "status";
            case FAVOURITE:
                return "favourite";
            case ANDROID_ID:
                return "_id";
            case BRANCH:
                return "branch";
            case TIME:
                return "time";
            default:
                throw new IllegalArgumentException();
        }
    }
}
