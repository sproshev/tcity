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
import android.content.ContentValues
import com.tcity.android.db.Schema
import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import com.tcity.android.R
import com.tcity.android.app.Application
import android.view.MenuItem
import com.tcity.android.rest.getProjectWebUrl
import com.tcity.android.app.Preferences
import com.tcity.android.rest.getBuildConfigurationWebUrl
import com.tcity.android.rest.getBuildWebUrl
import com.tcity.android.db.getBoolean
import com.tcity.android.db.dbValue
import com.tcity.android.concept.Status
import com.tcity.android.loader.getAndRunnablesChain
import com.tcity.android.loader.getProjectStatusRunnable
import com.tcity.android.loader.getBuildConfigurationStatusRunnable
import com.tcity.android.app.DB
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import kotlin.properties.Delegates
import android.widget.Toast
import android.widget.TextView
import com.tcity.android.loader.ChainListener

private abstract class BaseOverviewActivity : ListActivity(), OverviewListener {

    protected val PROJECT_ID_INTENT_KEY: String = "PROJECT_ID"
    protected val BUILD_CONFIGURATION_ID_INTENT_KEY: String = "BUILD_CONFIGURATION_ID"

    private var layout: SwipeRefreshLayout by Delegates.notNull()
    private var engine: OverviewEngine by Delegates.notNull()

    protected var application: Application by Delegates.notNull()
    protected var chainListener: GlobalChainListener by Delegates.notNull()

    // Lifecycle - BEGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ListActivity>.onCreate(savedInstanceState)

        setContentView(R.layout.overview)
        getActionBar().setTitle(calculateTitle())

        layout = findViewById(R.id.overview_layout) as SwipeRefreshLayout
        layout.setColorSchemeResources(R.color.green_status, R.color.red_status)
        layout.setOnRefreshListener { loadAllData() }

        application = getApplication() as Application

        engine = calculateEngine()

        chainListener = getLastNonConfigurationInstance() as GlobalChainListener? ?: GlobalChainListener()
        chainListener.activity = this

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

    protected abstract fun calculateTitle(): String
    protected abstract fun calculateEngine(): OverviewEngine
    protected abstract fun loadAllData()

    // OverviewListener - BEGIN

    override fun onProjectWatchClick(id: String) {
        onConceptWatchClick(id, Schema.PROJECT, ::getProjectStatusRunnable)
    }

    override fun onBuildConfigurationWatchClick(id: String) {
        onConceptWatchClick(id, Schema.BUILD_CONFIGURATION, ::getBuildConfigurationStatusRunnable)
    }

    override fun onBuildWatchClick(id: String) {
        throw UnsupportedOperationException()
    }

    override fun onProjectNameClick(id: String) {
        val intent = Intent(this, javaClass<ProjectActivity>())
        intent.putExtra(PROJECT_ID_INTENT_KEY, id)

        startActivity(intent)
    }

    override fun onBuildConfigurationNameClick(id: String) {
        val intent = Intent(this, javaClass<ProjectActivity>())
        intent.putExtra(BUILD_CONFIGURATION_ID_INTENT_KEY, id)

        startActivity(intent)
    }

    override fun onBuildNameClick(id: String) {
        throw UnsupportedOperationException()
    }

    override fun onProjectOptionsClick(id: String, anchor: View) {
        onConceptOptionsClick(id, anchor, ::getProjectWebUrl)
    }

    override fun onBuildConfigurationOptionsClick(id: String, anchor: View) {
        onConceptOptionsClick(id, anchor, ::getBuildConfigurationWebUrl)
    }

    override fun onBuildOptionsClick(id: String, anchor: View) {
        onConceptOptionsClick(id, anchor, ::getBuildWebUrl)
    }

    // OverviewListener - END

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

    private fun onConceptWatchClick(id: String, schema: Schema, runnable: (String, DB, Preferences) -> Runnable) {
        val watched = isConceptWatched(id, schema)

        updateConceptStatusAndWatched(id, schema, watched)

        if (!watched) {
            val statusChain = getAndRunnablesChain(
                    listOf(
                            runnable(
                                    id,
                                    application.getDB(),
                                    application.getPreferences()
                            )
                    ),
                    chainListener
            )

            chainListener.onStarted()
            statusChain.execute()
        }
    }

    private fun isConceptWatched(id: String, schema: Schema): Boolean {
        val cursor = application.getDB().query(
                schema,
                array(Schema.WATCHED_COLUMN),
                "${Schema.TC_ID_COLUMN} = ?",
                array(id)
        )

        cursor.moveToNext()

        val result = getBoolean(cursor, Schema.WATCHED_COLUMN)

        cursor.close()

        return result
    }

    private fun updateConceptStatusAndWatched(id: String, schema: Schema, watched: Boolean) {
        val values = ContentValues()
        values.put(Schema.STATUS_COLUMN, Status.DEFAULT.dbValue)
        values.put(Schema.WATCHED_COLUMN, (!watched).dbValue)

        application.getDB().update(
                schema,
                values,
                "${Schema.TC_ID_COLUMN} = ?",
                array(id)
        )
    }

    private fun onConceptOptionsClick(
            id: String,
            anchor: View,
            url: (String, Preferences) -> String
    ) {
        val menu = PopupMenu(this, anchor)

        menu.inflate(R.menu.menu_concept)

        menu.setOnMenuItemClickListener(ConceptMenuListener(id, url))

        menu.show()
    }

    private inner class ConceptMenuListener(
            private val id: String,
            private val url: (String, Preferences) -> String
    ) : PopupMenu.OnMenuItemClickListener {

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
                    url(id, application.getPreferences())
            )

            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share)))
        }

        private fun onDetailsClick() {
            // TODO implement
        }
    }

    protected class GlobalChainListener : ChainListener {

        public var activity: BaseOverviewActivity? = null

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
}
