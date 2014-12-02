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

import com.commonsware.cwac.merge.MergeAdapter
import android.view.View
import android.widget.TextView
import android.content.Context
import android.view.LayoutInflater
import com.tcity.android.R
import android.database.Cursor
import com.tcity.android.db.BuildSchema
import com.tcity.android.db.WATCHED_COLUMN
import com.tcity.android.db.contentValue
import com.tcity.android.db.BuildConfigurationSchema
import com.tcity.android.db.ProjectSchema
import com.tcity.android.app.DB

private class OverviewEngine(
        private val context: Context,
        private val db: DB,
        private val projectsSectionName: String, // TODO add parent selection
        private val buildConfigurationsSectionName: String, // TODO add parent selection
        private val buildsSectionName: String // TODO add parent selection
) {

    public var listener: OverviewListener? = null

    public val adapter: MergeAdapter = MergeAdapter()

    private val watchedBuildsHeader: TextView
    private val watchedBuildConfigurationsHeader: TextView
    private val watchedProjectsHeader: TextView

    private val projectsHeader: TextView
    private val buildConfigurationsHeader: TextView
    private val buildsHeader: TextView

    private val watchedBuildsCursor: Cursor
    private val watchedBuildConfigurationsCursor: Cursor
    private val watchedProjectsCursor: Cursor

    private val projectsCursor: Cursor
    private val buildConfigurationsCursor: Cursor
    private val buildsCursor: Cursor

    private val projectListener: ConceptListener
    private val buildConfigurationListener: ConceptListener
    private val buildListener: ConceptListener

    private val watchedBuildsAdapter: ConceptsCursorAdapter
    private val watchedBuildConfigurationsAdapter: ConceptsCursorAdapter
    private val watchedProjectsAdapter: ConceptsCursorAdapter

    private val projectsAdapter: ConceptsCursorAdapter
    private val buildConfigurationsAdapter: ConceptsCursorAdapter
    private val buildsAdapter: ConceptsCursorAdapter

    {
        val inflater = LayoutInflater.from(context)
        val watchedPrefix = context.getResources().getString(R.string.watched)

        watchedBuildsHeader = inflater.inflate(R.layout.separator_item, null, false) as TextView // TODO parent
        watchedBuildsHeader.setText("$watchedPrefix $buildsSectionName")

        watchedBuildConfigurationsHeader = inflater.inflate(R.layout.separator_item, null, false) as TextView // TODO parent
        watchedBuildConfigurationsHeader.setText("$watchedPrefix $buildConfigurationsSectionName")

        watchedProjectsHeader = inflater.inflate(R.layout.separator_item, null, false) as TextView // TODO parent
        watchedProjectsHeader.setText("$watchedPrefix $projectsSectionName")

        buildsHeader = inflater.inflate(R.layout.separator_item, null, false) as TextView // TODO parent
        buildsHeader.setText(buildsSectionName)

        buildConfigurationsHeader = inflater.inflate(R.layout.separator_item, null, false) as TextView // TODO parent
        buildConfigurationsHeader.setText(buildConfigurationsSectionName)

        projectsHeader = inflater.inflate(R.layout.separator_item, null, false) as TextView // TODO parent
        projectsHeader.setText(projectsSectionName)

        watchedBuildsCursor = db.query(BuildSchema, null, "$WATCHED_COLUMN = ?", array(true.contentValue.toString()), null, null, null)
        watchedBuildConfigurationsCursor = db.query(BuildConfigurationSchema, null, "$WATCHED_COLUMN = ?", array(true.contentValue.toString()), null, null, null)
        watchedProjectsCursor = db.query(ProjectSchema, null, "$WATCHED_COLUMN = ?", array(true.contentValue.toString()), null, null, null)

        projectsCursor = db.query(ProjectSchema, null, null, null, null, null, null)
        buildConfigurationsCursor = db.query(BuildConfigurationSchema, null, null, null, null, null, null)
        buildsCursor = db.query(BuildSchema, null, null, null, null, null, null)

        projectListener = object : ConceptListener {
            override fun onWatchClick(id: String) = listener?.onProjectWatchClick(id)
            override fun onNameClick(id: String) = listener?.onProjectNameClick(id)
            override fun onOptionsClick(id: String, anchor: View) = listener?.onProjectOptionsClick(id, anchor)
        }

        buildConfigurationListener = object : ConceptListener {
            override fun onWatchClick(id: String) = listener?.onBuildConfigurationWatchClick(id)
            override fun onNameClick(id: String) = listener?.onBuildConfigurationNameClick(id)
            override fun onOptionsClick(id: String, anchor: View) = listener?.onBuildConfigurationOptionsClick(id, anchor)
        }

        buildListener = object : ConceptListener {
            override fun onWatchClick(id: String) = listener?.onBuildWatchClick(id)
            override fun onNameClick(id: String) = listener?.onBuildNameClick(id)
            override fun onOptionsClick(id: String, anchor: View) = listener?.onBuildOptionsClick(id, anchor)
        }

        watchedBuildsAdapter = ConceptsCursorAdapter(context, buildListener)
        watchedBuildsAdapter.changeCursor(watchedBuildsCursor)

        watchedBuildConfigurationsAdapter = ConceptsCursorAdapter(context, buildConfigurationListener)
        watchedBuildConfigurationsAdapter.changeCursor(watchedBuildConfigurationsCursor)

        watchedProjectsAdapter = ConceptsCursorAdapter(context, projectListener)
        watchedProjectsAdapter.changeCursor(watchedProjectsCursor)

        projectsAdapter = ConceptsCursorAdapter(context, projectListener)
        projectsAdapter.changeCursor(projectsCursor)

        buildConfigurationsAdapter = ConceptsCursorAdapter(context, buildConfigurationListener)
        buildConfigurationsAdapter.changeCursor(buildConfigurationsCursor)

        buildsAdapter = ConceptsCursorAdapter(context, buildListener)
        buildsAdapter.changeCursor(buildsCursor)

        adapter.addView(watchedBuildsHeader)
        adapter.addAdapter(watchedBuildsAdapter)

        adapter.addView(watchedBuildConfigurationsHeader)
        adapter.addAdapter(watchedBuildConfigurationsAdapter)

        adapter.addView(watchedProjectsHeader)
        adapter.addAdapter(watchedProjectsAdapter)

        adapter.addView(projectsHeader)
        adapter.addAdapter(projectsAdapter)

        adapter.addView(buildConfigurationsHeader)
        adapter.addAdapter(buildConfigurationsAdapter)

        adapter.addView(buildsHeader)
        adapter.addAdapter(buildsAdapter)

        handleHeader(watchedBuildsHeader, watchedBuildsCursor)
        handleHeader(watchedBuildConfigurationsHeader, watchedBuildConfigurationsCursor)
        handleHeader(watchedProjectsHeader, watchedProjectsCursor)

        handleHeader(projectsHeader, projectsCursor)
        handleHeader(buildConfigurationsHeader, buildConfigurationsCursor)
        handleHeader(buildsHeader, buildsCursor)
    }

    public fun notifyProjectsChanged() {
        watchedProjectsCursor.requery() // TODO CursorLoader
        handleHeader(watchedProjectsHeader, watchedProjectsCursor)

        projectsCursor.requery() // TODO CursorLoader
        handleHeader(projectsHeader, projectsCursor)

        adapter.notifyDataSetChanged()
    }

    public fun notifyBuildConfigurationsChanged() {
        watchedBuildConfigurationsCursor.requery() // TODO CursorLoader
        handleHeader(watchedBuildConfigurationsHeader, watchedBuildConfigurationsCursor)

        buildConfigurationsCursor.requery() // TODO CursorLoader
        handleHeader(buildConfigurationsHeader, buildConfigurationsCursor)

        adapter.notifyDataSetChanged()
    }

    public fun notifyBuildsChanged() {
        watchedBuildsCursor.requery() // TODO CursorLoader
        handleHeader(watchedBuildsHeader, watchedBuildsCursor)

        buildsCursor.requery() // TODO CursorLoader
        handleHeader(buildsHeader, buildsCursor)

        adapter.notifyDataSetChanged()
    }

    private fun handleHeader(header: View, cursor: Cursor) {
        adapter.setActive(header, cursor.getCount() != 0)
    }
}
