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
import java.util.HashMap
import com.tcity.android.concept.rootProjectId
import com.tcity.android.R

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
        private val WATCHED_BUILDS_ID = 0
        private val WATCHED_BUILD_CONFIGURATIONS_ID = 1
        private val WATCHED_PROJECTS_ID = 2

        private val PROJECTS_ID = 3
        private val BUILD_CONFIGURATIONS_ID = 4
        private val BUILDS_ID = 5

        private val SEPARATOR_ID = 6
    }

    private val watchedPrefix = context.getResources().getString(R.string.watched)

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

            sizeUtils.setDataSize(WATCHED_PROJECTS_ID, watchedProjects.size)
            sizeUtils.setDataSize(PROJECTS_ID, this.projects.size)
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

            sizeUtils.setDataSize(WATCHED_BUILD_CONFIGURATIONS_ID, watchedBuildConfigurations.size)
            sizeUtils.setDataSize(BUILD_CONFIGURATIONS_ID, this.buildConfigurations.size)
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

            sizeUtils.setDataSize(WATCHED_BUILDS_ID, watchedBuilds.size)
            sizeUtils.setDataSize(BUILDS_ID, this.builds.size)
        }
    }

    override fun getCount() = sizeUtils.size
    override fun getItemId(position: Int) = position.toLong()
    override fun areAllItemsEnabled() = false
    override fun isEnabled(position: Int) = sizeUtils.getSectionAndIndex(position).second != -1
    override fun getViewTypeCount() = 7

    override fun getItem(position: Int): Any? {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)

        if (isSeparator(sectionAndIndex)) return null

        val index = sectionAndIndex.second

        return when (sectionAndIndex.first) {
            WATCHED_BUILDS_ID -> watchedBuilds[index]
            WATCHED_BUILD_CONFIGURATIONS_ID -> watchedBuildConfigurations[index]
            WATCHED_PROJECTS_ID -> watchedProjects[index]
            PROJECTS_ID -> projects[index]
            BUILD_CONFIGURATIONS_ID -> buildConfigurations[index]
            BUILDS_ID -> builds[index]
            else -> null
        }
    }

    override fun getItemViewType(position: Int): Int {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)

        if (isSeparator(sectionAndIndex)) return SEPARATOR_ID

        return sectionAndIndex.first
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)

        if (isSeparator(sectionAndIndex)) return viewUtils.getSeparatorView(sectionAndIndex.first, convertView, parent)

        val index = sectionAndIndex.second

        return when (sectionAndIndex.first) {
            WATCHED_PROJECTS_ID -> {
                viewUtils.getProjectView(watchedProjects[index], true, convertView, parent)
            }
            PROJECTS_ID -> {
                viewUtils.getProjectView(projects[index], false, convertView, parent)
            }
            else -> null
        }
    }

    private fun getSectionNames(): Map<Int, String> {
        val result = HashMap<Int, String>()

        if (buildsSectionName != null) {
            result.put(WATCHED_BUILDS_ID, "$watchedPrefix $buildsSectionName")
            result.put(BUILDS_ID, buildsSectionName)
        }

        if (buildConfigurationsSectionName != null) {
            result.put(WATCHED_BUILD_CONFIGURATIONS_ID, "$watchedPrefix $buildConfigurationsSectionName")
            result.put(BUILD_CONFIGURATIONS_ID, buildConfigurationsSectionName)
        }

        if (projectsSectionName != null) {
            result.put(WATCHED_PROJECTS_ID, "$watchedPrefix $projectsSectionName")
            result.put(PROJECTS_ID, projectsSectionName)
        }

        return result
    }

    private fun isSeparator(sectionAndIndex: Pair<Int, Int>) = sectionAndIndex.second == -1

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
