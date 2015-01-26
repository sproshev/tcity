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

import android.app.IntentService;
import android.content.Intent;

import com.tcity.android.app.Application;
import com.tcity.android.db.DB;

import org.jetbrains.annotations.NotNull;

public class SyncBoundService extends IntentService {

    @NotNull
    public static final String INTENT_KEY = "BUILD_CONFIGURATION_ID";

    public SyncBoundService() {
        super(SyncBoundService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@NotNull Intent intent) {
        DB db = ((Application) getApplication()).getDB();
        String buildConfigurationId = intent.getStringExtra(INTENT_KEY);

        db.setBuildConfigurationSyncBound(
                buildConfigurationId,
                System.currentTimeMillis()
        );
    }
}
