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
import com.tcity.android.loader.getBuildConfigurationsRunnable
import com.tcity.android.loader.getProjectsRunnable
import com.tcity.android.loader.RunnableChain

public class ProjectOverviewActivity : BaseOverviewActivity() {

    private var id: String by Delegates.notNull()

    private var projectsChain: RunnableChain by Delegates.notNull()
    private var executableProjectsChain: AsyncTask<Void, Exception, Void> by Delegates.notNull()

    private var buildConfigurationsChain: RunnableChain by Delegates.notNull()
    private var executableBuildConfigurationsChain: AsyncTask<Void, Exception, Void> by Delegates.notNull()

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseOverviewActivity>.onCreate(savedInstanceState)

        id = getIntent().getStringExtra(PROJECT_ID_INTENT_KEY)

        getActionBar().setTitle(calculateTitle())

        engine = calculateEngine()
        setListAdapter(engine.adapter)

        projectsChain = RunnableChain.getSingleRunnableChain(
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

        executableProjectsChain = projectsChain.toAsyncTask(chainListener)

        buildConfigurationsChain = RunnableChain.getSingleRunnableChain(
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

        executableBuildConfigurationsChain = buildConfigurationsChain.toAsyncTask(chainListener)

        if (chainListener.count == 0) {
            loadAllData()
        } else {
            updateRefreshing()
        }
    }

    // Lifecycle - END

    private fun calculateTitle(): String {
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

    private fun calculateEngine(): OverviewEngine {
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
        if (executableProjectsChain.getStatus() != Status.RUNNING) {
            if (executableProjectsChain.getStatus() == Status.FINISHED) {
                executableProjectsChain = projectsChain.toAsyncTask(chainListener)
            }

            chainListener.onStarted()
            executableProjectsChain.execute()
        }

        if (executableBuildConfigurationsChain.getStatus() != Status.RUNNING) {
            if (executableBuildConfigurationsChain.getStatus() == Status.FINISHED) {
                executableBuildConfigurationsChain = buildConfigurationsChain.toAsyncTask(chainListener)
            }

            chainListener.onStarted()
            executableBuildConfigurationsChain.execute()
        }
    }
}
