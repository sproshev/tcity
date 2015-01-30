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

class BuildArtifact {

    public final int size;

    @NotNull
    public final String name;

    @NotNull
    public final Type type;

    BuildArtifact(int size, @NotNull String name, @NotNull Type type) {
        this.size = size;
        this.name = name;
        this.type = type;
    }

    static enum Type {
        DIR, FILE, ARCHIVE
    }
}
