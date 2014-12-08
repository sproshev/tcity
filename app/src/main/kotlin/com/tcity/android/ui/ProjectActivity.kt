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

package com.tcity.android.ui

import kotlin.properties.Delegates
import android.os.Bundle
import com.tcity.android.R
import com.tcity.android.db.Schema
import android.os.AsyncTask.Status
import com.tcity.android.db.getName
import android.os.AsyncTask
import com.tcity.android.loader.getAndRunnablesChain
import com.tcity.android.loader.getBuildConfigurationsRunnable
import com.tcity.android.loader.getProjectsRunnable

public class ProjectActivity : BaseOverviewActivity() {

    private var id: String by Delegates.notNull()

    private var projectsRunnables: Collection<Runnable> by Delegates.notNull()
    private var projectsChain: AsyncTask<Void, Exception, Void> by Delegates.notNull()

    private var buildConfigurationsRunnables: Collection<Runnable> by Delegates.notNull()
    private var buildConfigurationsChain: AsyncTask<Void, Exception, Void> by Delegates.notNull()

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseOverviewActivity>.onCreate(savedInstanceState)

        id = getIntent().getStringExtra(PROJECT_ID_INTENT_KEY)

        projectsRunnables = listOf(
                getProjectsRunnable(
                        application.getDB(),
                        application.getPreferences()
                )/*,
                WatchedProjectStatusesRunnable(
                        application.getDB(),
                        application.getPreferences(),
                        id
                ) TODO */
        )

        projectsChain = getAndRunnablesChain(projectsRunnables, chainListener)

        buildConfigurationsRunnables = listOf(
                getBuildConfigurationsRunnable(
                        id,
                        application.getDB(),
                        application.getPreferences()
                )/*,
                WatchedBuildConfigurationStatusesRunnable(
                        application.getDB(),
                        application.getPreferences(),
                        id
                ) TODO */
        )

        buildConfigurationsChain = getAndRunnablesChain(buildConfigurationsRunnables, chainListener)
    }

    // Lifecycle - END

    override fun calculateTitle(): String {
        val cursor = application.getDB().query(
                Schema.PROJECT,
                array(Schema.NAME_COLUMN),
                "${Schema.TC_ID_COLUMN} = ?",
                array(id)
        )

        cursor.moveToNext()

        val result = getName(cursor)

        cursor.close()

        return result
    }

    override fun calculateEngine(): OverviewEngine {
        return OverviewEngine(
                this,
                application.getDB(),
                getListView(),
                getResources().getString(R.string.subprojects),
                getResources().getString(R.string.build_configurations),
                getResources().getString(R.string.builds), // TODO off
                this,
                id,
                id,
                id // TODO off
        )
    }

    override fun loadAllData() {
        if (projectsChain.getStatus() != Status.RUNNING) {
            if (projectsChain.getStatus() == Status.FINISHED) {
                projectsChain = getAndRunnablesChain(
                        projectsRunnables,
                        chainListener
                )
            }

            chainListener.onStarted()
            projectsChain.execute()
        }

        if (buildConfigurationsChain.getStatus() != Status.RUNNING) {
            if (buildConfigurationsChain.getStatus() == Status.FINISHED) {
                buildConfigurationsChain = getAndRunnablesChain(
                        buildConfigurationsRunnables,
                        chainListener
                )
            }

            chainListener.onStarted()
            buildConfigurationsChain.execute()
        }
    }
}
