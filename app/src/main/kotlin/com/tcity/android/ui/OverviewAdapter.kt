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

import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup
import com.tcity.android.concept.Project
import java.util.ArrayList
import java.util.Collections
import com.tcity.android.concept.BuildConfiguration
import com.tcity.android.concept.Build
import com.tcity.android.concept.Concept
import android.content.Context
import android.widget.Adapter
import java.util.HashMap
import com.tcity.android.concept.rootProjectId

class OverviewAdapter(
        context: Context,
        listener: OverviewListener,
        private val projectsSectionName: String? = null,
        private val buildConfigurationsSectionName: String? = null,
        private val buildsSectionName: String? = null,
        private val watchedProjectIds: Set<String> = Collections.emptySet(),
        private val watchedBuildConfigurationIds: Set<String> = Collections.emptySet(),
        private val watchedBuildIds: Set<String> = Collections.emptySet()
) : BaseAdapter() {

    class object {
        private val SEPARATOR_ITEM = 0
        private val PROJECT_ITEM = 1
        private val BUILD_CONFIGURATION_ITEM = 2
        private val BUILD_ITEM = 3

        private val WATCHED_BUILDS_POSITION = 0
        private val WATCHED_BUILD_CONFIGURATIONS_POSITION = 1
        private val WATCHED_PROJECTS_POSITION = 2
        private val PROJECTS_POSITION = 3
        private val BUILD_CONFIGURATIONS_POSITION = 4
        private val BUILDS_POSITION = 5
    }

    private val watchedBuilds: MutableList<Build> = ArrayList()
    private val watchedBuildConfigurations: MutableList<BuildConfiguration> = ArrayList()
    private val watchedProjects: MutableList<Project> = ArrayList()

    private val projects: MutableList<Project> = ArrayList()
    private val buildConfigurations: MutableList<BuildConfiguration> = ArrayList()
    private val builds: MutableList<Build> = ArrayList()

    private val sizeUtils = OverviewAdapterSizeUtils(Array<Int>(6, { 0 }))
    private val viewUtils = OverviewAdapterViewUtils(context, listener, getSectionNames())

    public fun updateProjects(projects: Collection<Project>) {
        if (projectsSectionName != null) {
            updateConcepts(
                    projects,
                    watchedProjects,
                    this.projects,
                    watchedProjectIds
            )

            sizeUtils.setDataSize(WATCHED_PROJECTS_POSITION, watchedProjects.size)
            sizeUtils.setDataSize(PROJECTS_POSITION, this.projects.size)
        }
    }

    public fun updateBuildConfigurations(buildConfigurations: Collection<BuildConfiguration>) {
        if (buildConfigurationsSectionName != null) {
            updateConcepts(
                    buildConfigurations,
                    watchedBuildConfigurations,
                    this.buildConfigurations,
                    watchedBuildConfigurationIds
            )

            sizeUtils.setDataSize(WATCHED_BUILD_CONFIGURATIONS_POSITION, watchedBuildConfigurations.size)
            sizeUtils.setDataSize(BUILD_CONFIGURATIONS_POSITION, this.buildConfigurations.size)
        }
    }

    public fun updateBuilds(builds: Collection<Build>) {
        if (buildsSectionName != null) {
            updateConcepts(
                    builds,
                    watchedBuilds,
                    this.builds,
                    watchedBuildIds
            )

            sizeUtils.setDataSize(WATCHED_BUILDS_POSITION, watchedBuilds.size)
            sizeUtils.setDataSize(BUILDS_POSITION, this.builds.size)
        }
    }

    override fun getCount() = sizeUtils.size
    override fun getItemId(position: Int) = position.toLong()
    override fun areAllItemsEnabled() = false
    override fun isEnabled(position: Int) = sizeUtils.getSectionAndIndex(position).second != -1
    override fun getViewTypeCount() = 4

    override fun getItem(position: Int): Any? {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)
        val index = sectionAndIndex.second

        if (index == -1) return null

        return when (sectionAndIndex.first) {
            WATCHED_BUILDS_POSITION -> watchedBuilds[index]
            WATCHED_BUILD_CONFIGURATIONS_POSITION -> watchedBuildConfigurations[index]
            WATCHED_PROJECTS_POSITION -> watchedProjects[index]
            PROJECTS_POSITION -> projects[index]
            BUILD_CONFIGURATIONS_POSITION -> buildConfigurations[index]
            BUILDS_POSITION -> builds[index]
            else -> null
        }
    }

    override fun getItemViewType(position: Int): Int {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)
        val index = sectionAndIndex.second

        if (index == -1) return SEPARATOR_ITEM

        return when (sectionAndIndex.first) {
            WATCHED_BUILDS_POSITION, BUILDS_POSITION -> BUILD_ITEM
            WATCHED_BUILD_CONFIGURATIONS_POSITION, BUILD_CONFIGURATIONS_POSITION -> BUILD_CONFIGURATION_ITEM
            WATCHED_PROJECTS_POSITION, PROJECTS_POSITION -> PROJECT_ITEM
            else -> Adapter.IGNORE_ITEM_VIEW_TYPE
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)
        val index = sectionAndIndex.second

        if (index == -1) return viewUtils.getSeparatorView(sectionAndIndex.first, convertView, parent)

        return when (sectionAndIndex.first) {
            WATCHED_PROJECTS_POSITION -> {
                viewUtils.getProjectView(watchedProjects[index], true, convertView, parent)
            }
            PROJECTS_POSITION -> {
                viewUtils.getProjectView(projects[index], false, convertView, parent)
            }
            else -> null
        }
    }

    private fun getSectionNames(): Map<Int, String> {
        val result = HashMap<Int, String>()
        val watched = "Watched" // TODO

        if (buildsSectionName != null) {
            result.put(WATCHED_BUILDS_POSITION, "$watched $buildsSectionName")
            result.put(BUILDS_POSITION, buildsSectionName)
        }

        if (buildConfigurationsSectionName != null) {
            result.put(WATCHED_BUILD_CONFIGURATIONS_POSITION, "$watched $buildConfigurationsSectionName")
            result.put(BUILD_CONFIGURATIONS_POSITION, buildConfigurationsSectionName)
        }

        if (projectsSectionName != null) {
            result.put(WATCHED_PROJECTS_POSITION, "$watched $projectsSectionName")
            result.put(PROJECTS_POSITION, projectsSectionName)
        }

        return result
    }

    private fun <T : Concept> updateConcepts(
            new: Collection<T>,
            watched: MutableList<T>,
            all: MutableList<T>,
            watchedIds: Set<String>
    ) {
        watched.clear()
        all.clear()

        new.forEach {
            val conceptId = it.id

            if (!conceptId.equals(rootProjectId)) {
                if (watchedIds.contains(conceptId)) {
                    watched.add(it)
                }

                all.add(it)
            }
        }
    }
}
