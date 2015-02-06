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

package com.tcity.android.ui.fav.overview;

import android.content.Context;
import android.database.Cursor;

import com.tcity.android.db.DB;
import com.tcity.android.ui.common.overview.ConceptAdapter;
import com.tcity.android.ui.common.overview.ConceptClickListener;

import org.jetbrains.annotations.NotNull;

class FavBuildConfigurationDBEngine {

    @NotNull
    private final ConceptAdapter myAdapter;

    @NotNull
    private final Cursor myCursor;

    public FavBuildConfigurationDBEngine(@NotNull Context context,
                                         @NotNull DB db,
                                         @NotNull ConceptClickListener clickListener) {
        myCursor = db.getBuildConfigurations(null, true);

        myAdapter = new FavBuildConfigurationAdapter(context, clickListener, db);
        myAdapter.changeCursor(myCursor);
    }

    @NotNull
    public ConceptAdapter getAdapter() {
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
