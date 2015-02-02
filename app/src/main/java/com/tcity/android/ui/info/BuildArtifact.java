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

package com.tcity.android.ui.info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildArtifact {

    public final long size;

    @NotNull
    public final String name;

    @Nullable
    public final String contentHref;

    @Nullable
    public final String childrenHref;

    BuildArtifact(long size,
                  @NotNull String name,
                  @Nullable String contentHref,
                  @Nullable String childrenHref) {
        this.size = size;
        this.name = name;
        this.contentHref = contentHref;
        this.childrenHref = childrenHref;
    }
}
