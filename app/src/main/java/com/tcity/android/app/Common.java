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

package com.tcity.android.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tcity.android.R;
import com.tcity.android.Status;

import org.jetbrains.annotations.NotNull;

public class Common {

    @NotNull
    public static final String TEAMCITY_DATE_FORMAT = "yyyyMMdd'T'HHmmssZ";

    private Common() {
    }

    public static boolean isNetworkAvailable(@NotNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static int loadTextColor(@NotNull Status status, @NotNull Context context) {
        return context.getResources().getColor(loadColorResource(status, android.R.color.black));
    }

    public static int loadBackgroundColor(@NotNull Status status, @NotNull Context context) {
        return context.getResources().getColor(loadColorResource(status, android.R.color.transparent));
    }

    private static int loadColorResource(@NotNull Status status, int defaultValue) {
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
                return defaultValue;
        }
    }
}
