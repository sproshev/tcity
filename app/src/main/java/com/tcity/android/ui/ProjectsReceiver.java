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
import android.widget.Toast;

import com.tcity.android.Receiver;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collection;

class ProjectsReceiver extends Receiver<Collection<Project>> {

    @NotNull
    private final WeakReference<Context> myContextWeakReference;

    @NotNull
    private final WeakReference<OverviewAdapter> myAdapterWeakReference;

    ProjectsReceiver(@NotNull Context context, @NotNull OverviewAdapter overviewAdapter) {
        myContextWeakReference = new WeakReference<>(context);
        myAdapterWeakReference = new WeakReference<>(overviewAdapter);
    }

    @Override
    public void handleResult(@NotNull Collection<Project> projects) {
        OverviewAdapter adapter = myAdapterWeakReference.get();

        if (adapter != null) {
            adapter.updateProjects(projects);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void handleException(@NotNull Exception e) {
        Context context = myContextWeakReference.get();

        if (context != null) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
