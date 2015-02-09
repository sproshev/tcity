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

package com.tcity.android.ui.common.overview;

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

public abstract class ConceptAdapter extends CursorAdapter {

    private static final int FAVOURITE_IMAGE = android.R.drawable.btn_star_big_on;
    private static final int NOT_FAVOURITE_IMAGE = android.R.drawable.btn_star_big_off;

    @NotNull
    private final Context myContext;

    @NotNull
    private final ConceptClickListener myClickListener;

    @NotNull
    private final String myFavouriteDescription;

    @NotNull
    private final String myNotFavouriteDescription;

    public ConceptAdapter(@NotNull Context context,
                          @NotNull ConceptClickListener clickListener,
                          int favouriteDescriptionId,
                          int notFavouriteDescriptionId) {
        super(context, null, true);

        myContext = context;
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
    public void bindView(@NotNull View view,
                         @NotNull Context context,
                         @NotNull Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        bindImage(holder.image, cursor);

        bindDescription(holder.description, cursor);
        bindName(holder.name, cursor);
        bindSub(holder.sub, cursor);

        bindOptions(holder.options, cursor);
    }

    protected void bindImage(@NotNull ImageButton view, @NotNull Cursor cursor) {
        final String id = DBUtils.getId(cursor);

        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myClickListener.onImageClick(id);
                    }
                }
        );

        if (DBUtils.isFavourite(cursor)) {
            view.setContentDescription(myFavouriteDescription);
            view.setImageResource(FAVOURITE_IMAGE);
        } else {
            view.setContentDescription(myNotFavouriteDescription);
            view.setImageResource(NOT_FAVOURITE_IMAGE);
        }
    }

    protected void bindDescription(@NotNull LinearLayout view, @NotNull Cursor cursor) {
        final String id = DBUtils.getId(cursor);

        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myClickListener.onDescriptionClick(id);
                    }
                }
        );
    }

    protected void bindName(@NotNull TextView view, @NotNull Cursor cursor) {
        view.setText(DBUtils.getName(cursor));
        view.setTextColor(
                Common.loadTextColor(DBUtils.getStatus(cursor), myContext)
        );
    }

    protected void bindSub(@NotNull TextView view, @NotNull Cursor cursor) {
    }

    private void bindOptions(@NotNull View view, @NotNull Cursor cursor) {
        final String id = DBUtils.getId(cursor);
        final View options = view;

        options.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myClickListener.onOptionsClick(id, options);
                    }
                }
        );
    }

    private static class ViewHolder {

        @NotNull
        private final ImageButton image;

        @NotNull
        private final LinearLayout description;

        @NotNull
        private final TextView name;

        @NotNull
        private final TextView sub;

        @NotNull
        private final View options;

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
