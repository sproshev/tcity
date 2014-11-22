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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tcity.android.R
import android.content.Context
import com.tcity.android.concept.Project
import android.widget.ImageButton

class OverviewAdapterViewUtils(
        context: Context,
        private val listener: OverviewListener,
        private val sectionNames: Map<Int, String>
) {

    class object {
        private val WATCHED_IMAGE = android.R.drawable.star_on
        private val NOT_WATCHED_IMAGE = android.R.drawable.star_off
    }

    private val inflater = LayoutInflater.from(context)

    public fun getSeparatorView(section: Int, convertView: View?, parent: ViewGroup?): View {
        val result: TextView

        if (convertView == null) {
            result = inflater.inflate(R.layout.separator_item, parent, false) as TextView
        } else {
            result = convertView as TextView
        }

        result.setText(sectionNames[section])

        return result
    }

    public fun getProjectView(project: Project, watched: Boolean, convertView: View?, parent: ViewGroup?): View {
        val result: View

        if (convertView == null) {
            result = inflater.inflate(R.layout.concept_item, parent, false)
        } else {
            result = convertView
        }

        val name = result.findViewById(R.id.concept_item_name) as TextView
        name.setText(project.name)

        val projectId = project.id

        val watch = result.findViewById(R.id.concept_item_watch) as ImageButton

        watch.setOnClickListener { listener.onChangeProjectWatch(projectId) }
        watch.setImageResource(
                if (watched) WATCHED_IMAGE else NOT_WATCHED_IMAGE
        )

        val options = result.findViewById(R.id.concept_item_options)
        options.setOnClickListener { listener.onProjectOptionsClick(projectId) }

        return result
    }
}
