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
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.Status;
import com.tcity.android.db.DBUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ProjectOrBuildConfigurationAdapter extends CursorAdapter {

    private static final int FAVOURITE_IMAGE = android.R.drawable.btn_star_big_on;
    private static final int NOT_FAVOURITE_IMAGE = android.R.drawable.btn_star_big_off;

    @NotNull
    private final ProjectOrBuildConfigurationClickListener myClickListener;

    @NotNull
    private final String myFavouriteDescription;

    @NotNull
    private final String myNotFavouriteDescription;

    ProjectOrBuildConfigurationAdapter(@NotNull Context context,
                                       @NotNull ProjectOrBuildConfigurationClickListener clickListener,
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
        View result = LayoutInflater.from(context).inflate(
                R.layout.project_or_build_configuration_item,
                parent,
                false
        );

        result.setTag(
                new ViewHolder(
                        (ImageButton) result.findViewById(R.id.project_or_build_configuration_favourite),
                        (TextView) result.findViewById(R.id.project_or_build_configuration_name),
                        result.findViewById(R.id.project_or_build_configuration_options)
                )
        );

        return result;
    }

    @Override
    public void bindView(@NotNull View view, @NotNull Context context, @NotNull Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String id = DBUtils.getId(cursor);

        bindImage(holder.image, id, DBUtils.getFavourite(cursor));
        holder.options.setOnClickListener(new OptionsListener(myClickListener, id));

        bindName(
                holder.name,
                id,
                DBUtils.getName(cursor),
                DBUtils.getStatus(cursor),
                context
        );
    }

    private void bindImage(@NotNull ImageButton imageView,
                           @NotNull String id,
                           boolean favourite) {
        imageView.setOnClickListener(new ImageListener(myClickListener, id));

        if (favourite) {
            imageView.setContentDescription(myFavouriteDescription);
            imageView.setImageResource(FAVOURITE_IMAGE);
        } else {
            imageView.setContentDescription(myNotFavouriteDescription);
            imageView.setImageResource(NOT_FAVOURITE_IMAGE);
        }
    }

    private void bindName(@NotNull TextView nameView,
                          @NotNull String id,
                          @NotNull String name,
                          @NotNull Status status,
                          @NotNull Context context) {
        nameView.setText(name);
        nameView.setOnClickListener(new NameListener(myClickListener, id));
        nameView.setTextColor(AdapterUtils.loadColor(status, context));
    }

    private static class NameListener implements View.OnClickListener {

        @NotNull
        private final ProjectOrBuildConfigurationClickListener myClickListener;

        @NotNull
        private final String myId;

        private NameListener(@NotNull ProjectOrBuildConfigurationClickListener clickListener,
                             @NotNull String id) {
            myClickListener = clickListener;
            myId = id;
        }

        @Override
        public void onClick(@NotNull View v) {
            myClickListener.onNameClick(myId);
        }
    }

    private static class ImageListener implements View.OnClickListener {

        @NotNull
        private final ProjectOrBuildConfigurationClickListener myClickListener;

        @NotNull
        private final String myId;

        private ImageListener(@NotNull ProjectOrBuildConfigurationClickListener clickListener,
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
        private final ProjectOrBuildConfigurationClickListener myClickListener;

        @NotNull
        private final String myId;

        private OptionsListener(@NotNull ProjectOrBuildConfigurationClickListener clickListener,
                                @NotNull String id) {
            myClickListener = clickListener;
            myId = id;
        }

        @Override
        public void onClick(@NotNull View v) {
            myClickListener.onOptionsClick(myId, v);
        }
    }

    private static class ViewHolder {

        @NotNull
        public final ImageButton image;

        @NotNull
        public final TextView name;

        @NotNull
        public final View options;

        private ViewHolder(@NotNull ImageButton image,
                           @NotNull TextView name,
                           @NotNull View options) {
            this.image = image;
            this.name = name;
            this.options = options;
        }
    }
}
