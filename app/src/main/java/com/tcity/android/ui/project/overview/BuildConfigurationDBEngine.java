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
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.db.DB;
import com.tcity.android.ui.common.overview.ConceptClickListener;

import org.jetbrains.annotations.NotNull;

class BuildConfigurationDBEngine {

    @NotNull
    private final TextView myHeader;

    @NotNull
    private final CursorAdapter myAdapter;

    @NotNull
    private final Cursor myCursor;

    BuildConfigurationDBEngine(@NotNull String parentProjectId,
                                      boolean onlyFavourite,
                                      @NotNull Context context,
                                      @NotNull DB db,
                                      @NotNull ViewGroup root,
                                      @NotNull ConceptClickListener clickListener,
                                      @NotNull String title) {
        LayoutInflater inflater = LayoutInflater.from(context);

        myHeader = (TextView) inflater.inflate(R.layout.overview_separator, root, false);
        myHeader.setText(title);

        myCursor = db.getBuildConfigurations(parentProjectId, onlyFavourite);

        myAdapter = new BuildConfigurationAdapter(context, clickListener);
        myAdapter.changeCursor(myCursor);
    }

    @NotNull
    TextView getHeader() {
        return myHeader;
    }

    @NotNull
    ListAdapter getAdapter() {
        return myAdapter;
    }

    boolean empty() {
        return myCursor.getCount() == 0;
    }

    void requery() {
        //noinspection deprecation
        myCursor.requery();
    }

    void close() {
        myAdapter.changeCursor(null);
    }
}
