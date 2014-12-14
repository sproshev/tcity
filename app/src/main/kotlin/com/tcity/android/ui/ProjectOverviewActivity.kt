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
import com.tcity.android.db.watchedDbValue
import com.tcity.android.db.getId
import com.tcity.android.loader.getProjectStatusRunnable
import com.tcity.android.loader.getBuildConfigurationStatusRunnable

public class ProjectOverviewActivity : BaseOverviewActivity() {

    private var id: String by Delegates.notNull()

    private var executableProjectsChain: AsyncTask<Void, Exception, Void>? = null
    private var executableBuildConfigurationsChain: AsyncTask<Void, Exception, Void>? = null

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseOverviewActivity>.onCreate(savedInstanceState)

        id = getIntent().getStringExtra(PROJECT_ID_INTENT_KEY)

        getActionBar().setTitle(calculateTitle())

        engine = calculateEngine()
        setListAdapter(engine.adapter)

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
        if (executableProjectsChain == null) {
            executableProjectsChain = calculateExecutableProjectsChain()
        }

        if (executableProjectsChain!!.getStatus() != Status.RUNNING) {
            if (executableProjectsChain!!.getStatus() == Status.FINISHED) {
                executableProjectsChain = calculateExecutableProjectsChain()
            }

            chainListener.onStarted()
            executableProjectsChain!!.execute()
        }

        if (executableBuildConfigurationsChain == null) {
            executableBuildConfigurationsChain = calculateExecutableBuildConfigurationsChain()
        }

        if (executableBuildConfigurationsChain!!.getStatus() != Status.RUNNING) {
            if (executableBuildConfigurationsChain!!.getStatus() == Status.FINISHED) {
                executableBuildConfigurationsChain = calculateExecutableBuildConfigurationsChain()
            }

            chainListener.onStarted()
            executableBuildConfigurationsChain!!.execute()
        }
    }

    private fun calculateExecutableProjectsChain(): AsyncTask<Void, Exception, Void> {
        val projectsChain = RunnableChain.getSingleRunnableChain(
                getProjectsRunnable(
                        application.getDB(),
                        application.getPreferences()
                )
        )

        return RunnableChain.getAndRunnableChain(
                projectsChain,
                calculateProjectStatusesChain()
        ).toAsyncTask(chainListener)
    }

    private fun calculateProjectStatusesChain(): RunnableChain {
        val cursor = application.getDB().query(
                Schema.PROJECT,
                array(Schema.TC_ID_COLUMN),
                "${Schema.PARENT_ID_COLUMN} = ? AND ${Schema.WATCHED_COLUMN} = ?",
                array(id, true.watchedDbValue.toString())
        )

        val runnables = arrayOfNulls<Runnable>(cursor.getCount())
        var pos = 0

        while (cursor.moveToNext()) {
            runnables[pos] =
                    getProjectStatusRunnable(
                            getId(cursor),
                            application.getDB(),
                            application.getPreferences()
                    )

            pos++
        }

        cursor.close()

        return RunnableChain.getOrRunnableChain(*runnables)
    }

    private fun calculateExecutableBuildConfigurationsChain(): AsyncTask<Void, Exception, Void> {
        val buildConfigurationsChain = RunnableChain.getSingleRunnableChain(
                getBuildConfigurationsRunnable(
                        id,
                        application.getDB(),
                        application.getPreferences()
                )
        )

        return RunnableChain.getAndRunnableChain(
                buildConfigurationsChain,
                calculateBuildConfigurationStatusesChain()
        ).toAsyncTask(chainListener)
    }

    private fun calculateBuildConfigurationStatusesChain(): RunnableChain {
        val cursor = application.getDB().query(
                Schema.BUILD_CONFIGURATION,
                array(Schema.TC_ID_COLUMN),
                "${Schema.PARENT_ID_COLUMN} = ? AND ${Schema.WATCHED_COLUMN} = ?",
                array(id, true.watchedDbValue.toString())
        )

        val runnables = arrayOfNulls<Runnable>(cursor.getCount())
        var pos = 0

        while (cursor.moveToNext()) {
            runnables[pos] = getBuildConfigurationStatusRunnable(
                    getId(cursor),
                    application.getDB(),
                    application.getPreferences()
            )

            pos++
        }

        cursor.close()

        return RunnableChain.getOrRunnableChain(*runnables)
    }
}
