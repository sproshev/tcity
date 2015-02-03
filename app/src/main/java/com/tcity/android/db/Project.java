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

public class Project {

    @NotNull
    public static final String ROOT_PROJECT_ID = "_Root";

    @NotNull
    public final String id;

    @NotNull
    public final String name;

    @NotNull
    public final String parentProjectId;

    public final boolean archived;

    public Project(@NotNull String id,
                   @NotNull String name,
                   @NotNull String parentProjectId,
                   boolean archived) {
        this.id = id;
        this.name = name;
        this.parentProjectId = parentProjectId;
        this.archived = archived;
    }
}
