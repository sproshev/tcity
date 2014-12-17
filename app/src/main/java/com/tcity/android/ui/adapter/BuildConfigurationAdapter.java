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
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.concept.Status;
import com.tcity.android.db.DbPackage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildConfigurationAdapter extends CursorAdapter {

    private static final int WATCHED_IMAGE = android.R.drawable.star_big_on;
    private static final int NOT_WATCHED_IMAGE = android.R.drawable.star_big_off;

    @NotNull
    private final ClickListener myClickListener;

    @NotNull
    private final String myWatchedDescription;

    @NotNull
    private final String myNotWatchedDescription;

    public BuildConfigurationAdapter(@NotNull Context context,
                                     @NotNull ClickListener clickListener) {
        super(context, null, true);

        myClickListener = clickListener;

        myWatchedDescription = context.getString(R.string.build_configuration_is_watched);
        myNotWatchedDescription = context.getString(R.string.build_configuration_is_not_watched);
    }

    @NotNull
    @Override
    public View newView(@NotNull Context context,
                        @Nullable Cursor cursor,
                        @Nullable ViewGroup parent) {
        View result = LayoutInflater.from(context).inflate(R.layout.concept_item, parent, false);

        result.setTag(
                new ViewHolder(
                        (ImageButton) result.findViewById(R.id.concept_item_watch),
                        (TextView) result.findViewById(R.id.concept_item_name),
                        result.findViewById(R.id.concept_item_options)
                )
        );

        return result;
    }

    @Override
    public void bindView(@NotNull View view, @NotNull Context context, @NotNull Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String id = DbPackage.getId(cursor);
        Drawable background = calculateBackground(DbPackage.getStatus(cursor), context);

        bindImage(holder.image, id, DbPackage.getWatched(cursor), background);
        bindName(holder.name, id, DbPackage.getName(cursor), background);
        bindOptions(holder.options, id, background);
    }

    private void bindImage(@NotNull ImageButton imageView,
                           @NotNull String id,
                           boolean watched,
                           @Nullable Drawable background) {
        imageView.setOnClickListener(new ImageListener(myClickListener, id));

        if (watched) {
            imageView.setContentDescription(myWatchedDescription);
            imageView.setImageResource(WATCHED_IMAGE);
        } else {
            imageView.setContentDescription(myNotWatchedDescription);
            imageView.setImageResource(NOT_WATCHED_IMAGE);
        }

        //noinspection deprecation
        imageView.setBackgroundDrawable(background);
    }

    private void bindName(@NotNull TextView nameView,
                          @NotNull String id,
                          @NotNull String name,
                          @Nullable Drawable background) {
        nameView.setText(name);
        nameView.setOnClickListener(new NameListener(myClickListener, id));

        //noinspection deprecation
        nameView.setBackgroundDrawable(background);
    }

    private void bindOptions(@NotNull View optionsView,
                             @NotNull String id,
                             @Nullable Drawable background) {
        optionsView.setOnClickListener(new OptionsListener(myClickListener, id));

        //noinspection deprecation
        optionsView.setBackgroundDrawable(background);
    }

    @Nullable
    private Drawable calculateBackground(@NotNull Status status, @NotNull Context context) {
        Drawable background = loadBackground(status, context);

        if (background != null) {
            background.setAlpha(40);
        }

        return background;
    }

    @Nullable
    private Drawable loadBackground(@NotNull Status status, @NotNull Context context) {
        switch (status) {
            case SUCCESS:
                return context.getResources().getDrawable(R.color.green_status);
            case FAILURE:
                return context.getResources().getDrawable(R.color.red_status);
            default:
                return null;
        }
    }

    public static interface ClickListener {

        public void onImageClick(@NotNull String id);

        public void onNameClick(@NotNull String id);

        public void onOptionsClick(@NotNull String id, @NotNull View anchor);
    }

    private static class NameListener implements View.OnClickListener {

        @NotNull
        private final ClickListener myClickListener;

        @NotNull
        private final String myId;

        private NameListener(@NotNull ClickListener clickListener, @NotNull String id) {
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
        private final ClickListener myClickListener;

        @NotNull
        private final String myId;

        private ImageListener(@NotNull ClickListener clickListener, @NotNull String id) {
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
        private final ClickListener myClickListener;

        @NotNull
        private final String myId;

        private OptionsListener(@NotNull ClickListener clickListener, @NotNull String id) {
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
