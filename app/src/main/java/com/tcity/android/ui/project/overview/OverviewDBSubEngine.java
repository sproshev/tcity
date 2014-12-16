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

package com.tcity.android.ui.project.overview;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.app.DB;
import com.tcity.android.db.Schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class OverviewDBSubEngine {

    @NotNull
    private final TextView header;

    @NotNull
    private final Cursor cursor;

    @NotNull
    private final OverviewAdapter adapter;

    OverviewDBSubEngine(@NotNull Context context,
                        @NotNull DB db,
                        @NotNull ViewGroup root,
                        @NotNull OverviewAdapter.ClickListener clickListener,
                        @NotNull String title,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs) {
        LayoutInflater inflater = LayoutInflater.from(context);

        header = (TextView) inflater.inflate(R.layout.separator_item, root, false);
        header.setText(title);

        cursor = db.query(
                Schema.PROJECT,
                null,
                selection,
                selectionArgs,
                null, null, null, null
        );

        adapter = new OverviewAdapter(context, clickListener);
        adapter.changeCursor(cursor);
    }

    @NotNull
    public TextView getHeader() {
        return header;
    }

    @NotNull
    public OverviewAdapter getAdapter() {
        return adapter;
    }

    public boolean empty() {
        return cursor.getCount() == 0;
    }

    public void requery() {
        cursor.requery();
    }

    public void close() {
        adapter.changeCursor(null);
    }
}
