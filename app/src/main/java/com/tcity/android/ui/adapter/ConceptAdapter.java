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

package com.tcity.android.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.app.Common;
import com.tcity.android.db.DBUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class ConceptAdapter extends CursorAdapter {

    private static final int FAVOURITE_IMAGE = android.R.drawable.btn_star_big_on;
    private static final int NOT_FAVOURITE_IMAGE = android.R.drawable.btn_star_big_off;

    @NotNull
    private final ConceptClickListener myClickListener;

    @NotNull
    private final String myFavouriteDescription;

    @NotNull
    private final String myNotFavouriteDescription;

    ConceptAdapter(@NotNull Context context,
                   @NotNull ConceptClickListener clickListener,
                   int favouriteDescriptionId,
                   int notFavouriteDescriptionId) {
        super(context, null, true);

        myClickListener = clickListener;

        myFavouriteDescription = context.getString(favouriteDescriptionId);
        myNotFavouriteDescription = context.getString(notFavouriteDescriptionId);
    }

    @NotNull
    @Override
    public View newView(@NotNull Context context,
                        @Nullable Cursor cursor,
                        @Nullable ViewGroup parent) {
        View result = LayoutInflater.from(context).inflate(R.layout.concept_item, parent, false);

        result.setTag(
                new ViewHolder(
                        (ImageButton) result.findViewById(R.id.concept_favourite),
                        (LinearLayout) result.findViewById(R.id.concept_description_layout),
                        (TextView) result.findViewById(R.id.concept_name),
                        (TextView) result.findViewById(R.id.concept_sub),
                        result.findViewById(R.id.concept_options)
                )
        );

        return result;
    }

    @Override
    public void bindView(@NotNull View view, @NotNull Context context, @NotNull Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String id = DBUtils.getId(cursor);

        holder.name.setText(DBUtils.getName(cursor));
        holder.name.setTextColor(
                Common.loadTextColor(DBUtils.getStatus(cursor), context)
        );

        holder.image.setOnClickListener(new ImageListener(myClickListener, id));
        holder.description.setOnClickListener(new DescriptionListener(myClickListener, id));
        holder.options.setOnClickListener(new OptionsListener(myClickListener, id));

        if (DBUtils.isFavourite(cursor)) {
            holder.image.setContentDescription(myFavouriteDescription);
            holder.image.setImageResource(FAVOURITE_IMAGE);
        } else {
            holder.image.setContentDescription(myNotFavouriteDescription);
            holder.image.setImageResource(NOT_FAVOURITE_IMAGE);
        }

        bindViewHolder(holder, cursor);
    }

    abstract void bindViewHolder(@NotNull ViewHolder holder,
                                 @NotNull Cursor cursor);

    private static class DescriptionListener implements View.OnClickListener {

        @NotNull
        private final ConceptClickListener myClickListener;

        @NotNull
        private final String myId;

        private DescriptionListener(@NotNull ConceptClickListener clickListener,
                                    @NotNull String id) {
            myClickListener = clickListener;
            myId = id;
        }

        @Override
        public void onClick(@NotNull View v) {
            myClickListener.onDescriptionClick(myId);
        }
    }

    private static class ImageListener implements View.OnClickListener {

        @NotNull
        private final ConceptClickListener myClickListener;

        @NotNull
        private final String myId;

        private ImageListener(@NotNull ConceptClickListener clickListener,
                              @NotNull String id) {
            myClickListener = clickListener;
            myId = id;
        }

        @Override
        public void onClick(@NotNull View v) {
            myClickListener.onImageClick(myId);
        }
    }

    private static class OptionsListener implements View.OnClickListener {

        @NotNull
        private final ConceptClickListener myClickListener;

        @NotNull
        private final String myId;

        private OptionsListener(@NotNull ConceptClickListener clickListener,
                                @NotNull String id) {
            myClickListener = clickListener;
            myId = id;
        }

        @Override
        public void onClick(@NotNull View v) {
            myClickListener.onOptionsClick(myId, v);
        }
    }

    static class ViewHolder {

        @NotNull
        final ImageButton image;

        @NotNull
        final LinearLayout description;

        @NotNull
        final TextView name;

        @NotNull
        final TextView sub;

        @NotNull
        final View options;

        private ViewHolder(@NotNull ImageButton image,
                           @NotNull LinearLayout description,
                           @NotNull TextView name,
                           @NotNull TextView sub,
                           @NotNull View options) {
            this.image = image;
            this.description = description;
            this.name = name;
            this.sub = sub;
            this.options = options;
        }
    }
}
