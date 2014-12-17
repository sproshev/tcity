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
import android.graphics.drawable.Drawable;

import com.tcity.android.R;
import com.tcity.android.concept.Status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Utils {

    private Utils() {
    }

    @Nullable
    public static Drawable getBackground(@NotNull Status status, @NotNull Context context) {
        Drawable background = loadBackground(status, context);

        if (background != null) {
            background.setAlpha(40);
        }

        return background;
    }

    @Nullable
    private static Drawable loadBackground(@NotNull Status status, @NotNull Context context) {
        switch (status) {
            case SUCCESS:
                return context.getResources().getDrawable(R.color.green);
            case FAILURE:
                return context.getResources().getDrawable(R.color.red);
            default:
                return null;
        }
    }
}
