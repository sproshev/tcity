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

package com.tcity.android.background.parser

import com.tcity.android.Status

public abstract class Concept(
        public val id: String,
        public val name: String,
        public val parentId: String,
        public val status: Status = Status.DEFAULT
)

public class Project(
        id: String,
        name: String,
        parentId: String
) : Concept(id, name, parentId)

public class BuildConfiguration(
        id: String,
        name: String,
        parentId: String
) : Concept(id, name, parentId)

public class Build(
        id: String,
        name: String,
        parentId: String,
        status: Status
) : Concept(id, name, parentId, status)