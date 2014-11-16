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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class OverviewCalculator {

    @NotNull
    private final Map<String, View> myProjectViews = new HashMap<>();

    @NotNull
    private final Context myContext;

    OverviewCalculator(@NotNull Context context) {
        myContext = context.getApplicationContext();
    }

    public void updateProjects(@NotNull Collection<Project> projects,
                               @NotNull ViewReceiver receiver) {
        LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout parent = (LinearLayout) inflater.inflate(R.layout.overview, null, false);

        TextView tv = (TextView) inflater.inflate(R.layout.separator_item, parent, false);
        tv.setText("ABC");
        parent.addView(tv);

        receiver.handleResult(parent);
    }
}
