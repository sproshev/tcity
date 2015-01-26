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

import com.tcity.android.R;
import com.tcity.android.Status;

import org.jetbrains.annotations.NotNull;

class AdapterUtils {

    private AdapterUtils() {
    }

    static int loadColor(@NotNull Status status, @NotNull Context context) {
        return context.getResources().getColor(loadColorResource(status));
    }

    private static int loadColorResource(@NotNull Status status) {
        switch (status) {
            case RUNNING:
                return R.color.blue;
            case SUCCESS:
                return R.color.green;
            case FAILURE:
            case ERROR:
            case WARNING:
                return R.color.red;
            default:
                return android.R.color.black;
        }
    }
}
