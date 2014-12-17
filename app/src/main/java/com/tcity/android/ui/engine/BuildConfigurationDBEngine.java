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
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.tcity.android.app.DB;
import com.tcity.android.db.Schema;
import com.tcity.android.ui.adapter.BuildConfigurationAdapter;
import com.tcity.android.ui.adapter.BuildConfigurationClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildConfigurationDBEngine extends DBEngine {

    @NotNull
    private final BuildConfigurationClickListener myClickListener;

    public BuildConfigurationDBEngine(@NotNull Context context,
                                      @NotNull DB db,
                                      @NotNull ViewGroup root,
                                      @NotNull BuildConfigurationClickListener clickListener,
                                      @NotNull String title,
                                      @Nullable String selection,
                                      @Nullable String[] selectionArgs) {
        super(context, db, root, title, Schema.BUILD_CONFIGURATION, selection, selectionArgs);

        myClickListener = clickListener;
    }

    @NotNull
    @Override
    protected CursorAdapter calculateAdapter(@NotNull Context context) {
        return new BuildConfigurationAdapter(context, myClickListener);
    }
}
