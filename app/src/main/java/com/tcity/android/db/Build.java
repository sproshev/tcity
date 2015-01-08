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

import com.tcity.android.Status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Build {

    @NotNull
    public final String id;

    @NotNull
    public final String name;

    @NotNull
    public final String parentBuildConfigurationId;

    @NotNull
    public final Status status;

    @Nullable
    public final String branch;

    public Build(@NotNull String id,
                 @NotNull String name,
                 @NotNull String parentBuildConfigurationId,
                 @NotNull Status status,
                 @Nullable String branch) {
        this.id = id;
        this.name = name;
        this.parentBuildConfigurationId = parentBuildConfigurationId;
        this.status = status;
        this.branch = branch;
    }
}
