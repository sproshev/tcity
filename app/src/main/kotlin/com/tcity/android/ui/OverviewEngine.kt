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
import android.content.Context
import com.tcity.android.R
import com.tcity.android.app.DB
import com.tcity.android.db.Schema
import android.view.ViewGroup
import com.tcity.android.db.SchemaListener
import android.os.Handler
import android.os.Message


private class OverviewEngine(
        context: Context,
        private val db: DB,
        root: ViewGroup,
        projectsSectionName: String,
        buildConfigurationsSectionName: String,
        buildsSectionName: String,
        listener: OverviewListener,
        projectsParentId: String? = null,
        buildConfigurationsParentId: String? = null,
        buildsParentId: String? = null
) {

    public val adapter: MergeAdapter = MergeAdapter()

    private val watchedPrefix = context.getResources().getString(R.string.watched)

    private val projectsEngine: OverviewConceptsEngine
    private val buildConfigurationsEngine: OverviewConceptsEngine
    private val buildsEngine: OverviewConceptsEngine

    private val projectSchemaListener: SchemaListener
    private val buildConfigurationSchemaListener: SchemaListener
    private val buildSchemaListener: SchemaListener

    {
        val projectViewListener = object : ConceptListener {
            override fun onWatchClick(id: String) = listener.onProjectWatchClick(id)
            override fun onNameClick(id: String) = listener.onProjectNameClick(id)
            override fun onOptionsClick(id: String, anchor: View) = listener.onProjectOptionsClick(id, anchor)
        }

        projectsEngine = OverviewConceptsEngine(context, db, root, Schema.PROJECT, projectViewListener, projectsSectionName, watchedPrefix, projectsParentId)

        val buildConfigurationViewListener = object : ConceptListener {
            override fun onWatchClick(id: String) = listener.onBuildConfigurationWatchClick(id)
            override fun onNameClick(id: String) = listener.onBuildConfigurationNameClick(id)
            override fun onOptionsClick(id: String, anchor: View) = listener.onBuildConfigurationOptionsClick(id, anchor)
        }

        buildConfigurationsEngine = OverviewConceptsEngine(context, db, root, Schema.BUILD_CONFIGURATION, buildConfigurationViewListener, buildConfigurationsSectionName, watchedPrefix, buildConfigurationsParentId)

        val buildViewListener = object : ConceptListener {
            override fun onWatchClick(id: String) = listener.onBuildWatchClick(id)
            override fun onNameClick(id: String) = listener.onBuildNameClick(id)
            override fun onOptionsClick(id: String, anchor: View) = listener.onBuildOptionsClick(id, anchor)
        }

        buildsEngine = OverviewConceptsEngine(context, db, root, Schema.BUILD, buildViewListener, buildsSectionName, watchedPrefix, buildsParentId)

        adapter.addView(buildsEngine.watchedHeader)
        adapter.addAdapter(buildsEngine.watchedAdapter)

        adapter.addView(buildConfigurationsEngine.watchedHeader)
        adapter.addAdapter(buildConfigurationsEngine.watchedAdapter)

        adapter.addView(projectsEngine.watchedHeader)
        adapter.addAdapter(projectsEngine.watchedAdapter)

        adapter.addView(projectsEngine.header)
        adapter.addAdapter(projectsEngine.adapter)

        adapter.addView(buildConfigurationsEngine.header)
        adapter.addAdapter(buildConfigurationsEngine.adapter)

        adapter.addView(buildsEngine.header)
        adapter.addAdapter(buildsEngine.adapter)

        adapter.setActive(buildsEngine.watchedHeader, !buildsEngine.watchedEmpty)
        adapter.setActive(buildConfigurationsEngine.watchedHeader, !buildConfigurationsEngine.watchedEmpty)
        adapter.setActive(projectsEngine.watchedHeader, !projectsEngine.watchedEmpty)

        adapter.setActive(projectsEngine.header, !projectsEngine.empty)
        adapter.setActive(buildConfigurationsEngine.header, !buildConfigurationsEngine.empty)
        adapter.setActive(buildsEngine.header, !buildsEngine.empty)

        projectSchemaListener = ConceptSchemaListener(projectsEngine, adapter)
        buildConfigurationSchemaListener = ConceptSchemaListener(buildConfigurationsEngine, adapter)
        buildSchemaListener = ConceptSchemaListener(buildsEngine, adapter)

        db.addListener(Schema.PROJECT, projectSchemaListener)
        db.addListener(Schema.BUILD_CONFIGURATION, buildConfigurationSchemaListener)
        db.addListener(Schema.BUILD, buildSchemaListener)
    }

    public fun onDestroy() {
        projectsEngine.onDestroy()
        buildConfigurationsEngine.onDestroy()
        buildsEngine.onDestroy()

        db.removeListener(Schema.PROJECT, projectSchemaListener)
        db.removeListener(Schema.BUILD_CONFIGURATION, buildConfigurationSchemaListener)
        db.removeListener(Schema.BUILD, buildSchemaListener)
    }

    private class ConceptSchemaListener(
            private val engine: OverviewConceptsEngine,
            private val adapter: MergeAdapter
    ) : SchemaListener {

        private val handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super<Handler>.handleMessage(msg)

                engine.requery()

                adapter.setActive(engine.watchedHeader, !engine.watchedEmpty)
                adapter.setActive(engine.header, !engine.empty)

                adapter.notifyDataSetChanged()
            }
        }

        override fun onChanged() {
            handler.sendEmptyMessage(0)
        }
    }
}
