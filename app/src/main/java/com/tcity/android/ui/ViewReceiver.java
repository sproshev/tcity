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

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.tcity.android.Receiver;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

class ViewReceiver extends Receiver<View> {

    @NotNull
    private final WeakReference<Activity> myActivityWeakReference;

    ViewReceiver(@NotNull Activity activity) {
        myActivityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void handleResult(@NotNull View view) {
        Activity activity = myActivityWeakReference.get();

        if (activity != null) {
            activity.setContentView(view);
        }
    }

    @Override
    public void handleException(@NotNull Exception e) {
        Activity activity = myActivityWeakReference.get();

        if (activity != null) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
