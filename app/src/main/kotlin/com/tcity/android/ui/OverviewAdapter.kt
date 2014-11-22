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
import android.view.LayoutInflater
import android.widget.Adapter

class OverviewAdapter(
        context: Context,
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
    }

    private val inflater = LayoutInflater.from(context)

    private val watchedBuilds: MutableList<Build> = ArrayList() // section 0
    private val watchedBuildConfigurations: MutableList<BuildConfiguration> = ArrayList() // section 1
    private val watchedProjects: MutableList<Project> = ArrayList() // section 2

    private val projects: MutableList<Project> = ArrayList() // section 3
    private val buildConfigurations: MutableList<BuildConfiguration> = ArrayList() // section 4
    private val builds: MutableList<Build> = ArrayList() // section 5

    private val sizeUtils = OverviewAdapterSizeUtils()
    private val viewUtils = OverviewAdapterViewUtils(context, getSectionNames())

    public fun updateProjects(projects: Collection<Project>) {
        if (projectsSectionName != null) {
            updateConcepts(
                    projects,
                    watchedProjects,
                    this.projects,
                    watchedProjectIds
            )

            sizeUtils.section2DataSize = watchedProjects.size
            sizeUtils.section3DataSize = this.projects.size
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

            sizeUtils.section1DataSize = watchedBuildConfigurations.size
            sizeUtils.section4DataSize = this.buildConfigurations.size
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

            sizeUtils.section0DataSize = watchedBuilds.size
            sizeUtils.section5DataSize = this.builds.size
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
            0 -> watchedBuilds[index]
            1 -> watchedBuildConfigurations[index]
            2 -> watchedProjects[index]
            3 -> projects[index]
            4 -> buildConfigurations[index]
            5 -> builds[index]
            else -> null
        }
    }

    override fun getItemViewType(position: Int): Int {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)
        val index = sectionAndIndex.second

        if (index == -1) return SEPARATOR_ITEM

        return when (sectionAndIndex.first) {
            0, 5 -> BUILD_ITEM
            1, 4 -> BUILD_CONFIGURATION_ITEM
            2, 3 -> PROJECT_ITEM
            else -> Adapter.IGNORE_ITEM_VIEW_TYPE
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val sectionAndIndex = sizeUtils.getSectionAndIndex(position)
        val index = sectionAndIndex.second

        if (index == -1) return viewUtils.getSeparatorView(sectionAndIndex.first, convertView, parent)

        return when (sectionAndIndex.first) {
            2, 3 -> {
                val project = projects[index]
                viewUtils.getProjectView(project, watchedProjectIds.contains(project.id), convertView, parent)
            }
            else -> null
        }
    }

    private fun getSectionNames(): List<String?> {
        val result = ArrayList<String?>()
        val watched = "Watched"

        if (buildsSectionName != null) {
            result.add("$watched $buildsSectionName")
        } else {
            result.add(null)
        }

        if (buildConfigurationsSectionName != null) {
            result.add("$watched $buildConfigurationsSectionName")
        } else {
            result.add(null)
        }

        if (projectsSectionName != null) {
            result.add("$watched $projectsSectionName")
        } else {
            result.add(null)
        }

        result.add(projectsSectionName)

        result.add(buildConfigurationsSectionName)

        result.add(buildsSectionName)

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
            if (watchedIds.contains(it.id)) {
                watched.add(it)
            }

            all.add(it)
        }
    }
}
