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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tcity.android.app.Preferences;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SyncUtils {

    public static final int MIN_INTERVAL = 15;
    public static final int MAX_INTERVAL = 720;
    public static final int DEFAULT_INTERVAL = 30;

    private SyncUtils() {
    }

    public static void initSync(@NotNull Context context, int intervalMinutes) {
        enableSync(context, intervalMinutes, true);

        Preferences preferences = new Preferences(context);
        preferences.setSyncEnabled(true);
        preferences.setSyncWifiOnly(true);
    }

    public static void enableSync(@NotNull Context context, int intervalMinutes, boolean wifiOnly) {
        enableReceiver(context);
        scheduleAlarm(context, intervalMinutes, wifiOnly);
    }

    public static void disableSync(@NotNull Context context) {
        unscheduleAlarm(context);
        disableReceiver(context);
    }

    public static void updateSyncConnection(@NotNull Context context,
                                            int intervalMinutes,
                                            boolean wifiOnly) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        Preferences preferences = new Preferences(context);

        if (preferences.isSyncScheduled()) {
            if (!isNetworkAvailable(networkInfo) ||
                    !isConnectionProper(wifiOnly, isWifi(networkInfo))) {
                unscheduleAlarm(context);
            }
        } else {
            scheduleAlarm(context, intervalMinutes, wifiOnly);
        }
    }

    public static void updateSyncInterval(@NotNull Context context,
                                          int intervalMinutes,
                                          boolean wifiOnly) {
        unscheduleAlarm(context);
        scheduleAlarm(context, intervalMinutes, wifiOnly);
    }

    private static void scheduleAlarm(@NotNull Context context,
                                      int intervalMinutes,
                                      boolean wifiOnly) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        Preferences preferences = new Preferences(context);

        if (isNetworkAvailable(networkInfo) &&
                isConnectionProper(wifiOnly, isWifi(networkInfo)) &&
                !preferences.isSyncScheduled()) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            manager.setInexactRepeating(
                    AlarmManager.RTC,
                    System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3,
                    intervalMinutes * 60 * 1000,
                    getPendingIntent(context)
            );

            preferences.setSyncScheduled(true);
        }
    }

    private static void unscheduleAlarm(@NotNull Context context) {
        Preferences preferences = new Preferences(context);

        if (preferences.isSyncScheduled()) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            manager.cancel(getPendingIntent(context));

            preferences.setSyncScheduled(false);
        }
    }

    private static void enableReceiver(@NotNull Context context) {
        Preferences preferences = new Preferences(context);

        if (!preferences.isSyncReceiverEnabled()) {
            setReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

            preferences.setSyncReceiverEnabled(true);
        }
    }

    private static void disableReceiver(@NotNull Context context) {
        Preferences preferences = new Preferences(context);

        if (preferences.isSyncReceiverEnabled()) {
            setReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

            preferences.setSyncReceiverEnabled(false);
        }
    }

    @Nullable
    private static NetworkInfo getNetworkInfo(@NotNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return manager.getActiveNetworkInfo();
    }

    private static boolean isWifi(@Nullable NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private static boolean isNetworkAvailable(@Nullable NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private static boolean isConnectionProper(boolean wifiOnly, boolean isWifi) {
        return wifiOnly && isWifi || !wifiOnly;
    }

    @NotNull
    private static PendingIntent getPendingIntent(@NotNull Context context) {
        return PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, SyncReceiver.class),
                0
        );
    }

    private static void setReceiverState(@NotNull Context context, int state) {
        ComponentName receiver = new ComponentName(context, SyncReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(
                receiver,
                state,
                PackageManager.DONT_KILL_APP
        );
    }
}
