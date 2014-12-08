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

import android.app.ListActivity
import android.support.v4.widget.SwipeRefreshLayout
import kotlin.properties.Delegates
import com.tcity.android.app.Application
import android.os.Bundle
import com.tcity.android.R
import com.tcity.android.loader.ProjectsRunnable
import com.tcity.android.loader.status.WatchedProjectStatusesRunnable
import com.tcity.android.loader.RunnablesChain
import com.tcity.android.loader.AndRunnablesChain
import com.tcity.android.loader.ChainListener
import com.tcity.android.loader.status.WatchedBuildConfigurationStatusesRunnable
import android.os.AsyncTask.Status
import android.widget.Toast
import android.view.View
import com.tcity.android.db.Schema
import com.tcity.android.db.dbValues
import android.widget.PopupMenu
import android.view.MenuItem
import android.content.Intent
import com.tcity.android.rest.getProjectWebUrl
import android.content.ContentValues
import com.tcity.android.db.dbValue
import com.tcity.android.loader.status.ProjectStatusRunnable
import android.widget.TextView

public class MainActivity : ListActivity(), OverviewListener {

    private var layout: SwipeRefreshLayout by Delegates.notNull()
    private var application: Application by Delegates.notNull()
    private var engine: OverviewEngine by Delegates.notNull()

    private var chainListener: GlobalChainListener by Delegates.notNull()

    private var projectsRunnables: Collection<Runnable> by Delegates.notNull()
    private var projectsChain: RunnablesChain by Delegates.notNull()

    private var buildConfigurationsRunnables: Collection<Runnable> by Delegates.notNull()
    private var buildConfigurationsChain: RunnablesChain by Delegates.notNull()

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ListActivity>.onCreate(savedInstanceState)

        setContentView(R.layout.overview)

        getActionBar().setTitle(R.string.overview)

        layout = findViewById(R.id.overview_layout) as SwipeRefreshLayout
        layout.setColorSchemeResources(R.color.green_status, R.color.red_status)
        layout.setOnRefreshListener { loadAllData() }

        application = getApplication() as Application

        engine = OverviewEngine(
                this,
                application.getDB(),
                getListView(),
                getResources().getString(R.string.projects),
                getResources().getString(R.string.build_configurations),
                getResources().getString(R.string.builds),
                this
        )

        chainListener = getLastNonConfigurationInstance() as GlobalChainListener? ?: GlobalChainListener()
        chainListener.activity = this

        projectsRunnables = listOf(
                ProjectsRunnable(
                        application.getDB(),
                        application.getPreferences()
                ),
                WatchedProjectStatusesRunnable(
                        application.getDB(),
                        application.getPreferences()
                )
        )

        projectsChain = AndRunnablesChain(chainListener, projectsRunnables)

        buildConfigurationsRunnables = listOf(
                WatchedBuildConfigurationStatusesRunnable(
                        application.getDB(),
                        application.getPreferences()
                )
        )

        buildConfigurationsChain = AndRunnablesChain(chainListener, buildConfigurationsRunnables)

        getListView().setAdapter(engine.adapter)

        if (chainListener.count == 0) {
            loadAllData()
        } else {
            updateRefreshing()
        }
    }

    override fun onRetainNonConfigurationInstance() = chainListener

    override fun onDestroy() {
        super<ListActivity>.onDestroy()

        engine.onDestroy()
        chainListener.activity = null
    }

    // Lifecycle - END

    // OverviewListener - BEGIN

    override fun onProjectWatchClick(id: String) {
        val watched = application.getPreferences().getWatchedProjectIds().contains(id)

        if (watched) {
            application.getPreferences().removeWatchedProjectId(id)
        } else {
            application.getPreferences().addWatchedProjectId(id)
        }

        val values = ContentValues()
        values.putAll(com.tcity.android.concept.Status.DEFAULT.dbValues)
        values.put(Schema.WATCHED_COLUMN, (!watched).dbValue)

        application.getDB().update(
                Schema.PROJECT,
                values,
                "${Schema.TC_ID_COLUMN} = ?",
                array(id)
        )

        if (!watched) {
            val projectStatusChain = AndRunnablesChain(
                    chainListener,
                    listOf(
                            ProjectStatusRunnable(id, application.getDB(), application.getPreferences())
                    )
            )

            chainListener.onStarted()
            projectStatusChain.execute()
        }
    }

    override fun onBuildConfigurationWatchClick(id: String) {
        throw UnsupportedOperationException()
    }

    override fun onBuildWatchClick(id: String) {
        throw UnsupportedOperationException()
    }

    override fun onProjectNameClick(id: String) {
        val intent = Intent(this, javaClass<ProjectActivity>())
        intent.putExtra("PROJECT_ID", id)

        startActivity(intent)
    }

    override fun onBuildConfigurationNameClick(id: String) {
        throw UnsupportedOperationException()
    }

    override fun onBuildNameClick(id: String) {
        throw UnsupportedOperationException()
    }

    override fun onProjectOptionsClick(id: String, anchor: View) {
        val menu = PopupMenu(this, anchor)

        menu.inflate(R.menu.menu_concept)

        menu.setOnMenuItemClickListener(ProjectMenuListener(id))

        menu.show()
    }

    override fun onBuildConfigurationOptionsClick(id: String, anchor: View) {
        throw UnsupportedOperationException()
    }

    override fun onBuildOptionsClick(id: String, anchor: View) {
        throw UnsupportedOperationException()
    }

    // OverviewListener - END

    private fun loadAllData() {
        if (projectsChain.getStatus() != Status.RUNNING) {
            if (projectsChain.getStatus() == Status.FINISHED) {
                projectsChain = AndRunnablesChain(
                        chainListener,
                        projectsRunnables
                )
            }

            chainListener.onStarted()
            projectsChain.execute()
        }

        if (buildConfigurationsChain.getStatus() != Status.RUNNING) {
            if (buildConfigurationsChain.getStatus() == Status.FINISHED) {
                buildConfigurationsChain = AndRunnablesChain(
                        chainListener,
                        buildConfigurationsRunnables
                )
            }

            chainListener.onStarted()
            buildConfigurationsChain.execute()
        }
    }

    private fun updateRefreshing() {
        if (layout.isRefreshing() && chainListener.count == 0) {
            layout.setRefreshing(false)
            (getListView().getEmptyView() as TextView).setText(R.string.empty)
        }

        if (!layout.isRefreshing() && chainListener.count != 0) {
            layout.setRefreshing(true) // https://code.google.com/p/android/issues/detail?id=77712
            (getListView().getEmptyView() as TextView).setText(R.string.loading)
        }
    }

    private class GlobalChainListener : ChainListener {

        public var activity: MainActivity? = null

        private var mutableCount = 0

        public val count: Int
            get() = mutableCount

        public fun onStarted() {
            mutableCount++

            activity?.updateRefreshing()
        }

        override fun onFinished() {
            mutableCount--

            activity?.updateRefreshing()
        }

        override fun onException(e: Exception) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show()
        }
    }

    private inner class ProjectMenuListener(private val id: String) : PopupMenu.OnMenuItemClickListener {

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            return when (item?.getItemId()) {
                R.id.menu_share -> {
                    onShareClick()

                    true
                }
                R.id.menu_details -> {
                    onDetailsClick()

                    true
                }
                else -> false
            }
        }

        private fun onShareClick() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    getProjectWebUrl(id, application.getPreferences())
            )

            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share)))
        }

        private fun onDetailsClick() {
            // TODO implement
        }
    }
}
