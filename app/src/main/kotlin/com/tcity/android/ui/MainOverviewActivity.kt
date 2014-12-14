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

import android.os.Bundle
import com.tcity.android.R
import android.os.AsyncTask.Status
import com.tcity.android.loader.getProjectsRunnable
import com.tcity.android.concept.ROOT_PROJECT_ID
import android.os.AsyncTask
import com.tcity.android.loader.RunnableChain
import com.tcity.android.db.Schema
import com.tcity.android.db.watchedDbValue
import com.tcity.android.loader.getProjectStatusRunnable
import com.tcity.android.db.getId

public class MainOverviewActivity : BaseOverviewActivity() {

    private var executableChain: AsyncTask<Void, Exception, Void>? = null

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseOverviewActivity>.onCreate(savedInstanceState)

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

    private fun calculateTitle() = getResources().getString(R.string.overview)

    private fun calculateEngine(): OverviewEngine {
        return OverviewEngine(
                this,
                db,
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
        if (executableChain == null) {
            executableChain = calculateExecutableChain()
        }

        if (executableChain!!.getStatus() != Status.RUNNING) {
            if (executableChain!!.getStatus() == Status.FINISHED) {
                executableChain = calculateExecutableChain()
            }

            chainListener.onStarted()
            executableChain!!.execute()
        }
    }

    private fun calculateExecutableChain(): AsyncTask<Void, Exception, Void> {
        val projectsChain = RunnableChain.getSingleRunnableChain(
                getProjectsRunnable(db, preferences)
        )

        return RunnableChain.getAndRunnableChain(
                projectsChain,
                calculateStatusesChain()
        ).toAsyncTask(chainListener)
    }

    private fun calculateStatusesChain(): RunnableChain {
        val cursor = db.query(
                Schema.PROJECT,
                array(Schema.TC_ID_COLUMN),
                "${Schema.PARENT_ID_COLUMN} = ? AND ${Schema.WATCHED_COLUMN} = ?",
                array(ROOT_PROJECT_ID, true.watchedDbValue.toString())
        )

        val runnables = arrayOfNulls<Runnable>(cursor.getCount())
        var pos = 0

        while (cursor.moveToNext()) {
            runnables[pos] = getProjectStatusRunnable(
                    getId(cursor), db, preferences
            )

            pos++
        }

        cursor.close()

        return RunnableChain.getOrRunnableChain(*runnables)
    }
}
