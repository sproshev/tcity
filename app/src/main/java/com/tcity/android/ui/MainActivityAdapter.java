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

package com.tcity.android.ui;

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

import java.util.List;
import java.util.Map;

class MainActivityAdapter extends BaseAdapter {

    @NotNull
    public static final String TYPE_KEY = "type";

    @NotNull
    public static final String CONCEPT_FOLLOW_KEY = "follow";

    @NotNull
    public static final String CONCEPT_NAME_KEY = "name";

    @NotNull
    public static final String CONCEPT_ID_KEY = "id";

    @NotNull
    public static final String SEPARATOR_TEXT_KEY = "text";

    public static final int CONCEPT_ITEM = 0;
    public static final int SEPARATOR_ITEM = 1;

    private static final int TYPES = 2;

    @NotNull
    private final List<Map<String, Object>> myData;

    @NotNull
    private final LayoutInflater myInflater;

    MainActivityAdapter(@NotNull Context context, @NotNull List<Map<String, Object>> data) {
        myInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myData = data;
    }

    @Override
    public int getViewTypeCount() {
        return TYPES;
    }

    @Override
    public int getItemViewType(int position) {
        return getIntValue(position, TYPE_KEY);
    }

    @Override
    public int getCount() {
        return myData.size();
    }

    @Override
    @NotNull
    public Map<String, Object> getItem(int position) {
        return myData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
        int type = getItemViewType(position);

        if (convertView == null) {
            if (type == CONCEPT_ITEM) {
                convertView = myInflater.inflate(R.layout.concept_item, parent, false);
            } else {
                convertView = myInflater.inflate(R.layout.separator_item, parent, false);
            }
        }

        if (type == CONCEPT_ITEM) {
            fillConceptItem(position, convertView);
        } else {
            fillSeparatorItem(position, convertView);
        }

        return convertView;
    }

    private int getIntValue(int position, @NotNull String key) {
        return (int) myData.get(position).get(key);
    }

    @NotNull
    private String getStringValue(int position, @NotNull String key) {
        return (String) myData.get(position).get(key);
    }

    private void fillConceptItem(int position, @NotNull View convertView) {
        ImageButton follow = (ImageButton) convertView.findViewById(R.id.concept_item_follow);
        follow.setImageResource(getIntValue(position, CONCEPT_FOLLOW_KEY));

        TextView name = (TextView) convertView.findViewById(R.id.concept_item_name);
        name.setText(getStringValue(position, CONCEPT_NAME_KEY));
    }

    private void fillSeparatorItem(int position, @NotNull View convertView) {
        TextView text = (TextView) convertView.findViewById(R.id.separator_item_text);
        text.setText(getStringValue(position, SEPARATOR_TEXT_KEY));
    }
}
