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

import android.database.Cursor
import com.tcity.android.db.Schema
import com.tcity.android.db.dbValue
import com.tcity.android.app.DB
import android.content.Context
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.tcity.android.R

private class OverviewConceptsEngine(
        context: Context,
        db: DB,
        private val root: ViewGroup,
        schema: Schema,
        listener: ConceptListener,
        name: String,
        watchedPrefix: String
) {
    private val inflater = LayoutInflater.from(context)

    private val watchedCursor: Cursor
    private val cursor: Cursor

    public val watchedHeader: TextView = calculateHeader("$watchedPrefix $name")
    public val header: TextView = calculateHeader(name)

    public val watchedAdapter: ConceptsCursorAdapter
    public val adapter: ConceptsCursorAdapter

    public val watchedEmpty: Boolean
        get() = watchedCursor.getCount() == 0

    public val empty: Boolean
        get() = cursor.getCount() == 0

    {
        watchedCursor = db.query(
                schema,
                null,
                "${Schema.WATCHED_COLUMN} = ?",
                array(true.dbValue.toString())
        )

        cursor = db.query(schema)

        watchedAdapter = ConceptsCursorAdapter(context, listener)
        watchedAdapter.changeCursor(watchedCursor)

        adapter = ConceptsCursorAdapter(context, listener)
        adapter.changeCursor(cursor)
    }

    public fun requery() {
        watchedCursor.requery()
        cursor.requery()
    }

    public fun onDestroy() {
        watchedAdapter.changeCursor(null)
        adapter.changeCursor(null)
    }

    private fun calculateHeader(text: String): TextView {
        val header = inflater.inflate(R.layout.separator_item, root, false) as TextView

        header.setText(text)

        return header
    }
}
