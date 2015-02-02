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

package com.tcity.android.ui.info;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tcity.android.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class BuildArtifactsAdapter extends BaseAdapter {

    @NotNull
    private final LayoutInflater myInflater;

    @NotNull
    private final BuildArtifactListener myListener;

    @NotNull
    private List<BuildArtifact> myData = Collections.emptyList();

    BuildArtifactsAdapter(@NotNull Context context, @NotNull BuildArtifactListener listener) {
        super();

        myInflater = LayoutInflater.from(context);
        myListener = listener;
    }

    void setData(@NotNull List<BuildArtifact> data) {
        myData = data;
    }

    @Override
    public int getCount() {
        return myData.size();
    }

    @Override
    public BuildArtifact getItem(int position) {
        return myData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        BuildArtifact artifact = getItem(position);

        if (artifact.childrenHref != null) {
            if (artifact.contentHref == null) {
                return 0; // dir
            } else {
                return 1; // archive
            }
        } else {
            if (artifact.contentHref == null) {
                throw new IllegalStateException(
                        "Invalid build artifact: " +
                                "content href and children href can't be null at the same time"
                );
            } else {
                return 2; // file
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
        switch (getItemViewType(position)) {
            case 0:
                return getDirView(position, convertView, parent);
            case 1:
                return getArchiveView(position, convertView, parent);
            case 2:
                return getFileView(position, convertView, parent);
            default:
                throw new IllegalStateException(
                        "Unexpected item view type: " + getItemViewType(position)
                );
        }
    }

    @NotNull
    private View getDirView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.build_artifact_dir_item, parent, false);
        }

        final BuildArtifact artifact = getItem(position);

        TextView textView = (TextView) convertView.findViewById(R.id.build_artifact_name);
        textView.setText(artifact.name);

        textView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myListener.onDescriptionClick(artifact);
                    }
                }
        );

        return convertView;
    }

    @NotNull
    private View getArchiveView(int position,
                                @Nullable View convertView,
                                @NotNull ViewGroup parent) {
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.build_artifact_archive_item, parent, false);
        }

        final BuildArtifact artifact = getItem(position);

        TextView textView = (TextView) convertView.findViewById(R.id.build_artifact_name);
        textView.setText(artifact.name);

        textView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myListener.onDescriptionClick(artifact);
                    }
                }
        );

        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.build_artifact_dl);
        imageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myListener.onDownloadClick(artifact);
                    }
                }
        );

        return convertView;
    }

    @NotNull
    private View getFileView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.build_artifact_file_item, parent, false);
        }

        final BuildArtifact artifact = getItem(position);

        TextView textView = (TextView) convertView.findViewById(R.id.build_artifact_name);
        textView.setText(artifact.name);

        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.build_artifact_dl);
        imageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        myListener.onDownloadClick(artifact);
                    }
                }
        );

        return convertView;
    }
}
