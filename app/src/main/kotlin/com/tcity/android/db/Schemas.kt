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

package com.tcity.android.db

public val TC_ID_COLUMN: String = "tc_id"
public val NAME_COLUMN: String = "name"
public val PARENT_ID_COLUMN: String = "parent_id"
public val STATUS_COLUMN: String = "status"
public val WATCHED_COLUMN: String = "watched"

public val BUILD_SCHEMA: BuildSchema = BuildSchema()
public val BUILD_CONFIGURATION_SCHEMA: BuildConfigurationSchema = BuildConfigurationSchema()
public val PROJECT_SCHEMA: ProjectSchema = ProjectSchema()

public trait Schema {
    public val tableName: String
    public val createScript: String
    public val dropScript: String
}

public abstract class ConceptSchema : Schema {
    public override val createScript: String =
            """
            CREATE TABLE $tableName (
                $TC_ID_COLUMN TEXT NOT NULL UNIQUE,
                $NAME_COLUMN TEXT NOT NULL,
                $PARENT_ID_COLUMN TEXT NOT NULL,
                $STATUS_COLUMN TEXT NOT NULL,
                $WATCHED_COLUMN INTEGER NOT NULL
            );
            """
    public override val dropScript: String = "DROP TABLE $tableName"
}

public class BuildSchema : ConceptSchema() {
    override val tableName: String = "Build"
}

public class BuildConfigurationSchema : ConceptSchema() {
    override val tableName: String = "BuildConfiguration"
}

public class ProjectSchema : ConceptSchema() {
    override val tableName: String = "Project"
}


