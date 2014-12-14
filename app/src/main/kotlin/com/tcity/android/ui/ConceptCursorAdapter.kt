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
import android.widget.CursorAdapter
import com.tcity.android.concept.Status
import com.tcity.android.db.getStatus
import android.graphics.drawable.Drawable
import com.tcity.android.db.getId
import com.tcity.android.db.getName
import com.tcity.android.db.getWatched

private class ConceptCursorAdapter(
        private val context: Context,
        private val listener: ConceptListener
) : CursorAdapter(context, null) {

    class object {
        private val WATCHED_IMAGE = android.R.drawable.star_big_on
        private val NOT_WATCHED_IMAGE = android.R.drawable.star_big_off
    }

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

        val conceptId = getId(cursor)

        holder.name.setText(getName(cursor))
        holder.name.setOnClickListener { listener.onNameClick(conceptId) }

        holder.watch.setOnClickListener { listener.onWatchClick(conceptId) }

        val watched = getWatched(cursor)

        if (watched) {
            holder.watch.setContentDescription(unwatchDescription)
            holder.watch.setImageResource(WATCHED_IMAGE)
        } else {
            holder.watch.setContentDescription(watchDescription)
            holder.watch.setImageResource(NOT_WATCHED_IMAGE)
        }

        holder.options.setOnClickListener { listener.onOptionsClick(conceptId, it) }

        val background = getStatus(cursor).background
        background?.setAlpha(40)

        holder.watch.setBackgroundDrawable(background)
        holder.name.setBackgroundDrawable(background)
        holder.options.setBackgroundDrawable(background)
    }

    private val Status.background: Drawable?
        get() {
            return when (this) {
                Status.FAILURE -> context.getResources().getDrawable(R.color.red_status)
                Status.SUCCESS -> context.getResources().getDrawable(R.color.green_status)
                else -> null
            }
        }

    private class ConceptViewHolder(public val name: TextView, public val watch: ImageButton, public val options: View)
}
