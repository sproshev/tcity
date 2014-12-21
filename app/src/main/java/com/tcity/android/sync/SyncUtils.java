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

import com.tcity.android.app.Preferences;

import org.jetbrains.annotations.NotNull;

public class SyncUtils {

    private SyncUtils() {
    }

    public static void enableSync(@NotNull Context context) {
        enableReceiver(context);
        scheduleReceiver(context);
    }

    public static void disableSync(@NotNull Context context) {
        unscheduleReceiver(context);
        disableReceiver(context);
    }

    static void scheduleReceiver(@NotNull Context context) {
        Preferences preferences = new Preferences(context);

        if (preferences.isSyncEnabled() && !preferences.isSyncScheduled()) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            manager.setInexactRepeating(
                    AlarmManager.RTC,
                    System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3,
                    AlarmManager.INTERVAL_HALF_HOUR,
                    getPendingIntent(context)
            );

            preferences.setSyncScheduled(true);
        }
    }

    static void unscheduleReceiver(@NotNull Context context) {
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

    private static void setReceiverState(@NotNull Context context, int state) {
        ComponentName receiver = new ComponentName(context, SyncReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(
                receiver,
                state,
                PackageManager.DONT_KILL_APP
        );
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
}
