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
import android.view.View;

import com.tcity.android.R;
import com.tcity.android.db.DBUtils;
import com.tcity.android.ui.common.overview.ConceptAdapter;
import com.tcity.android.ui.common.overview.ConceptClickListener;

import org.jetbrains.annotations.NotNull;

class BuildConfigurationAdapter extends ConceptAdapter {

    BuildConfigurationAdapter(@NotNull Context context,
                              @NotNull ConceptClickListener clickListener) {
        super(
                context,
                clickListener,
                R.string.build_configuration_was_marked_as_favourite,
                R.string.build_configuration_is_not_marked_as_favourite
        );
    }

    @Override
    protected void bindViewHolder(@NotNull ViewHolder holder,
                        @NotNull Cursor cursor) {
        if (!DBUtils.isPaused(cursor)) {
            holder.sub.setVisibility(View.GONE);
        } else {
            holder.sub.setVisibility(View.VISIBLE);
            holder.sub.setText(R.string.paused);
        }
    }
}
