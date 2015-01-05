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

package com.tcity.android.ui.engine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.db.DB;
import com.tcity.android.ui.adapter.ProjectAdapter;
import com.tcity.android.ui.adapter.ProjectClickListener;

import org.jetbrains.annotations.NotNull;

public class ProjectDBEngine {

    @NotNull
    private final TextView myHeader;

    @NotNull
    private final CursorAdapter myAdapter;

    @NotNull
    private final Cursor myCursor;

    public ProjectDBEngine(@NotNull String parentProjectId,
                           boolean onlyFavourite,
                           @NotNull Context context,
                           @NotNull DB db,
                           @NotNull ViewGroup root,
                           @NotNull ProjectClickListener clickListener,
                           @NotNull String title) {
        LayoutInflater inflater = LayoutInflater.from(context);

        myHeader = (TextView) inflater.inflate(R.layout.overview_separator, root, false);
        myHeader.setText(title);

        myCursor = db.getProjects(parentProjectId, onlyFavourite);

        myAdapter = new ProjectAdapter(context, clickListener);
        myAdapter.changeCursor(myCursor);
    }

    @NotNull
    public TextView getHeader() {
        return myHeader;
    }

    @NotNull
    public ListAdapter getAdapter() {
        return myAdapter;
    }

    public boolean empty() {
        return myCursor.getCount() == 0;
    }

    public void requery() {
        //noinspection deprecation
        myCursor.requery();
    }

    public void close() {
        myAdapter.changeCursor(null);
    }
}
