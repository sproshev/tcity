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

package com.tcity.android.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tcity.android.app.Preferences;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        switch (intent.getAction()) {
            case "android.intent.action.BOOT_COMPLETED":
                SyncUtils.scheduleReceiver(context);

                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":
                onConnectivityChange(context);

                break;
            default:
                onAlarm(context);

                break;
        }
    }

    private void onConnectivityChange(@NotNull Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            scheduleIfConnectionIsProper(context, networkInfo);
        } else {
            SyncUtils.unscheduleReceiver(context);
        }
    }

    @Nullable
    private NetworkInfo getNetworkInfo(@NotNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return manager.getActiveNetworkInfo();
    }

    private void scheduleIfConnectionIsProper(@NotNull Context context,
                                              @NotNull NetworkInfo networkInfo) {
        Preferences preferences = new Preferences(context);
        boolean isWifi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;

        if (preferences.isSyncWifiOnly() && isWifi || !preferences.isSyncWifiOnly()) {
            SyncUtils.scheduleReceiver(context);
        }
    }

    private void onAlarm(@NotNull Context context) {
        // TODO start service
    }
}
