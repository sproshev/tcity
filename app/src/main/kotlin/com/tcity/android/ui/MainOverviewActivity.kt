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
import android.os.AsyncTask.Status
import com.tcity.android.loader.getProjectsRunnable
import com.tcity.android.loader.getAndRunnablesChain
import com.tcity.android.concept.ROOT_PROJECT_ID
import android.os.AsyncTask

public class MainOverviewActivity : BaseOverviewActivity() {

    private var runnables: Collection<Runnable> by Delegates.notNull()
    private var chain: AsyncTask<Void, Exception, Void> by Delegates.notNull()

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseOverviewActivity>.onCreate(savedInstanceState)

        getActionBar().setTitle(calculateTitle())

        engine = calculateEngine()
        setListAdapter(engine.adapter)

        runnables = listOf(
                getProjectsRunnable(
                        application.getDB(),
                        application.getPreferences()
                )/*,
                WatchedProjectStatusesRunnable( // TODO stub
                        application.getDB(),
                        application.getPreferences()
                )
                */
        )

        chain = getAndRunnablesChain(runnables, chainListener)

        if (chainListener.count == 0) {
            loadAllData()
        } else {
            updateRefreshing()
        }
    }

    // Lifecycle - END

    private fun calculateTitle() = getResources().getString(R.string.overview)

    private fun calculateEngine(): OverviewEngine {
        return OverviewEngine(
                this,
                application.getDB(),
                getListView(),
                getResources().getString(R.string.projects),
                getResources().getString(R.string.build_configurations), // TODO off
                getResources().getString(R.string.builds), // TODO off
                this,
                ROOT_PROJECT_ID,
                ROOT_PROJECT_ID, // TODO off
                ROOT_PROJECT_ID // TODO off
        )
    }

    override fun loadAllData() {
        if (chain.getStatus() != Status.RUNNING) {
            if (chain.getStatus() == Status.FINISHED) {
                chain = getAndRunnablesChain(
                        runnables,
                        chainListener
                )
            }

            chainListener.onStarted()
            chain.execute()
        }
    }
}
