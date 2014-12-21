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

import org.jetbrains.annotations.NotNull;

public class SyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                SyncUtils.scheduleAlarm(context);
            } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                SyncUtils.updateAlarm(context);
            }
        } else {
            onAlarm(context);
        }
    }

    private void onAlarm(@NotNull Context context) {
        // TODO start service
    }
}
