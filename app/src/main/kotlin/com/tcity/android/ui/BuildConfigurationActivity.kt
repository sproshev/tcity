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
import com.tcity.android.loader.RunnablesChain
import android.os.Bundle
import com.tcity.android.R
import com.tcity.android.db.Schema
import com.tcity.android.db.getName
import android.os.AsyncTask.Status
import com.tcity.android.loader.getBuildsRunnable
import com.tcity.android.loader.getAndRunnablesChain

public class BuildConfigurationActivity : BaseOverviewActivity() {

    private var id: String by Delegates.notNull()

    private var runnables: Collection<Runnable> by Delegates.notNull()
    private var chain: RunnablesChain by Delegates.notNull()

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseOverviewActivity>.onCreate(savedInstanceState)

        id = getIntent().getStringExtra(BUILD_CONFIGURATION_ID_INTENT_KEY)

        runnables = listOf(
                getBuildsRunnable(
                        id,
                        application.getDB(),
                        application.getPreferences()
                )
        )

        chain = getAndRunnablesChain(runnables, chainListener)
    }

    // Lifecycle - END

    override fun calculateTitle(): String {
        val cursor = application.getDB().query(
                Schema.BUILD_CONFIGURATION,
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
                getResources().getString(R.string.subprojects), // TODO off
                getResources().getString(R.string.build_configurations), // TODO off
                getResources().getString(R.string.builds),
                this,
                id, // TODO off
                id, // TODO off
                id
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
