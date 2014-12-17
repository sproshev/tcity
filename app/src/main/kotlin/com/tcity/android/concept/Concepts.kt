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

package com.tcity.android.concept

public val ROOT_PROJECT_ID: String = "_Root"

public enum class Status {
    FAILURE
    SUCCESS
    UNKNOWN
    DEFAULT
}

public abstract class Concept(
        public val id: String,
        public val name: String,
        public val parentId: String,
        public val status: Status,
        public val watched: Boolean
)

public class Project(
        id: String,
        name: String,
        parentId: String,
        status: Status = Status.DEFAULT,
        watched: Boolean = false
) : Concept(id, name, parentId, status, watched)

public class BuildConfiguration(
        id: String,
        name: String,
        parentId: String,
        status: Status = Status.DEFAULT,
        watched: Boolean = false
) : Concept(id, name, parentId, status, watched)

public class Build(
        id: String,
        name: String,
        parentId: String,
        status: Status,
        watched: Boolean = false
) : Concept(id, name, parentId, status, watched)