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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.parser.ParserPackage;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.HttpStatusException;
import com.tcity.android.db.Build;
import com.tcity.android.db.DB;
import com.tcity.android.db.DBUtils;
import com.tcity.android.ui.overview.buildconfiguration.BuildConfigurationOverviewActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class SyncService extends IntentService {

    @NotNull
    private DB myDB;

    @NotNull
    private RestClient myClient;

    @NotNull
    private NotificationManager myManager;

    public SyncService() {
        super(SyncService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myDB = ((Application) getApplication()).getDB();
        myClient = new RestClient(new Preferences(this));
        myManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Cursor cursor = myDB.getBuildConfigurations(null, true);

        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                handleBuildConfiguration(cursor);
            }
        } finally {
            cursor.close();
        }
    }

    private void handleBuildConfiguration(@NotNull Cursor cursor) {
        try {
            String id = DBUtils.getId(cursor);

            long syncBound = myDB.getBuildConfigurationSyncBound(id);

            if (syncBound == DBUtils.UNDEFINED_TIME) {
                return;
            }

            HttpResponse response = myClient.getBuilds(id, syncBound);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpStatusException(statusLine);
            } else {
                handleResponse(id, DBUtils.getParentId(cursor), response);
            }
        } catch (IOException e) {
            Log.i(
                    SyncService.class.getSimpleName(), e.getMessage(), e
            );
        }
    }

    private void handleResponse(@NotNull String id,
                                @NotNull String parentId,
                                @NotNull HttpResponse response) throws IOException {
        List<Build> builds = ParserPackage.parseBuilds(response.getEntity().getContent());

        if (!builds.isEmpty()) {
            Notification.Builder builder = new Notification.Builder(this);

            String title = builds.size() + " new build" + (builds.size() == 1 ? "" : "s");
            String description = myDB.getProjectName(parentId) + " - " + myDB.getBuildConfigurationName(id);

            Intent activityIntent = new Intent(this, BuildConfigurationOverviewActivity.class);
            activityIntent.putExtra(BuildConfigurationOverviewActivity.INTENT_KEY, id);
            activityIntent.setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent contentIntent = PendingIntent.getActivity(
                    this,
                    0,
                    activityIntent,
                    0
            );

            Intent serviceIntent = new Intent(this, SyncBoundService.class);
            serviceIntent.putExtra(SyncBoundService.INTENT_KEY, id);
            serviceIntent.setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent deleteIntent = PendingIntent.getService(
                    this,
                    0,
                    serviceIntent,
                    0
            );

            //noinspection deprecation
            Notification notification = builder
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deleteIntent)
                    .setAutoCancel(true)
                    .getNotification();

            myManager.notify(id.hashCode(), notification);
        }
    }
}
