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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.db.DBUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildAdapter extends CursorAdapter {

    private static final int FAVOURITE_IMAGE = android.R.drawable.star_big_on;
    private static final int NOT_FAVOURITE_IMAGE = android.R.drawable.star_big_off;

    @NotNull
    private final BuildClickListener myClickListener;

    @NotNull
    private final String myFavouriteDescription;

    @NotNull
    private final String myNotFavouriteDescription;

    public BuildAdapter(@NotNull Context context, @NotNull BuildClickListener listener) {
        super(context, null, true);

        myClickListener = listener;

        myFavouriteDescription = context.getString(R.string.build_was_marked_as_favourite);
        myNotFavouriteDescription = context.getString(R.string.build_was_not_marked_as_favourite);
    }

    @NotNull
    @Override
    public View newView(@NotNull Context context,
                        @Nullable Cursor cursor,
                        @Nullable ViewGroup parent) {
        View result = LayoutInflater.from(context).inflate(R.layout.build_item, parent, false);

        result.setTag(
                new ViewHolder(
                        (ImageButton) result.findViewById(R.id.build_favourite),
                        (LinearLayout) result.findViewById(R.id.build_description_layout),
                        (TextView) result.findViewById(R.id.build_name),
                        (TextView) result.findViewById(R.id.build_branch),
                        result.findViewById(R.id.build_options)
                )
        );

        return result;
    }

    @Override
    public void bindView(@NotNull View view, @NotNull Context context, @NotNull Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String id = DBUtils.getId(cursor);
        Drawable background = AdapterUtils.getBackground(DBUtils.getStatus(cursor), context);

        bindImage(holder.image, id, DBUtils.getFavourite(cursor), background);
        bindDescription(holder.description, id, background);

        holder.name.setText(DBUtils.getName(cursor));

        bindBranch(holder.branch, DBUtils.getBranch(cursor));
        bindOptions(holder.options, id, background);
    }

    private void bindImage(@NotNull ImageButton imageView,
                           @NotNull String id,
                           boolean favourite,
                           @Nullable Drawable background) {
        imageView.setOnClickListener(new ImageListener(myClickListener, id));

        if (favourite) {
            imageView.setContentDescription(myFavouriteDescription);
            imageView.setImageResource(FAVOURITE_IMAGE);
        } else {
            imageView.setContentDescription(myNotFavouriteDescription);
            imageView.setImageResource(NOT_FAVOURITE_IMAGE);
        }

        //noinspection deprecation
        imageView.setBackgroundDrawable(background);
    }

    private void bindDescription(@NotNull LinearLayout layout,
                                 @NotNull String id,
                                 @Nullable Drawable background) {
        layout.setOnClickListener(new DescriptionListener(myClickListener, id));

        //noinspection deprecation
        layout.setBackgroundDrawable(background);
    }

    private void bindBranch(@NotNull TextView branchView,
                           @Nullable String branch) {
        if (branch == null) {
            branchView.setVisibility(View.GONE);
        } else {
            branchView.setVisibility(View.VISIBLE);
            branchView.setText(branch);
        }
    }

    private void bindOptions(@NotNull View optionsView,
                             @NotNull String id,
                             @Nullable Drawable background) {
        optionsView.setOnClickListener(new OptionsListener(myClickListener, id));

        //noinspection deprecation
        optionsView.setBackgroundDrawable(background);
    }

    private static class DescriptionListener implements View.OnClickListener {

        @NotNull
        private final BuildClickListener myClickListener;

        @NotNull
        private final String myId;

        private DescriptionListener(@NotNull BuildClickListener clickListener,
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
        private final BuildClickListener myClickListener;

        @NotNull
        private final String myId;

        private ImageListener(@NotNull BuildClickListener clickListener,
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
        private final BuildClickListener myClickListener;

        @NotNull
        private final String myId;

        private OptionsListener(@NotNull BuildClickListener clickListener,
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
        public final LinearLayout description;

        @NotNull
        public final TextView name;

        @NotNull
        public final TextView branch;

        @NotNull
        public final View options;

        private ViewHolder(@NotNull ImageButton image,
                           @NotNull LinearLayout description,
                           @NotNull TextView name,
                           @NotNull TextView branch,
                           @NotNull View options) {
            this.image = image;
            this.description = description;
            this.name = name;
            this.branch = branch;
            this.options = options;
        }
    }
}
