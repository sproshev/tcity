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

package com.tcity.android.loader.status

import com.tcity.android.app.Preferences
import com.tcity.android.rest.getProjectStatusUrl
import com.tcity.android.app.DB
import com.tcity.android.db.Schema


public class ProjectStatusRunnable(
        private val id: String,
        private val db: DB,
        private val preferences: Preferences
) : Runnable {

    override fun run() {
        saveStatus(
                id,
                loadStatus(getProjectStatusUrl(id, preferences), preferences),
                db,
                Schema.PROJECT
        )
    }
}
