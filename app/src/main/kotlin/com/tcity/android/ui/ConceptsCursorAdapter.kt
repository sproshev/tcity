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

import android.content.Context
import android.database.Cursor
import android.view.ViewGroup
import android.view.View
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.ImageButton
import com.tcity.android.R
import com.tcity.android.db.booleanValue
import android.widget.CursorAdapter
import com.tcity.android.concept.Status
import com.tcity.android.db.Schema

private class ConceptsCursorAdapter(
        private val context: Context,
        private val listener: ConceptListener
) : CursorAdapter(context, null) {

    private val watchDescription = context.getResources().getString(R.string.watch)
    private val unwatchDescription = context.getResources().getString(R.string.unwatch)

    private val inflater = LayoutInflater.from(context)

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        val result = inflater.inflate(R.layout.concept_item, parent, false)

        result.setTag(
                ConceptViewHolder(
                        result.findViewById(R.id.concept_item_name) as TextView,
                        result.findViewById(R.id.concept_item_watch) as ImageButton,
                        result.findViewById(R.id.concept_item_options)
                )
        )

        return result
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        val holder = view.getTag() as ConceptViewHolder

        val conceptId = cursor.getString(cursor.getColumnIndex(Schema.TC_ID_COLUMN))

        holder.name.setText(cursor.getString(cursor.getColumnIndex(Schema.NAME_COLUMN)))
        holder.name.setOnClickListener { listener.onNameClick(conceptId) }

        holder.watch.setOnClickListener { listener.onWatchClick(conceptId) }

        if (cursor.getInt(cursor.getColumnIndex(Schema.WATCHED_COLUMN)).booleanValue) {
            holder.watch.setContentDescription(unwatchDescription)

            holder.watch.setBackgroundResource(
                    Status.valueOf(
                            cursor.getString(cursor.getColumnIndex(Schema.STATUS_COLUMN))
                    ).color
            )
        } else {
            holder.watch.setContentDescription(watchDescription)

            holder.watch.setBackground(null)
        }

        holder.options.setOnClickListener { listener.onOptionsClick(conceptId, it) }
    }

    private val Status.color: Int
        get() {
            return when (this) {
                Status.FAILURE -> R.color.red_status
                Status.SUCCESS -> R.color.green_status
                else -> R.color.default_status
            }
        }

    private class ConceptViewHolder(public val name: TextView, public val watch: ImageButton, public val options: View)
}
