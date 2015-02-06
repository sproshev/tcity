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
import com.tcity.android.background.HttpStatusException;
import com.tcity.android.background.parser.ParserPackage;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.db.Build;
import com.tcity.android.db.DB;
import com.tcity.android.db.DBUtils;
import com.tcity.android.ui.buildconfiguration.overview.BuildConfigurationOverviewActivity;

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
            syncAll(cursor);
        } finally {
            cursor.close();
        }
    }

    private void syncAll(@NotNull Cursor cursor) {
        while (cursor.moveToNext()) {
            try {
                String id = DBUtils.getId(cursor);

                sync(id, DBUtils.getParentId(cursor), myDB.getBuildConfigurationSyncBound(id));
            } catch (IOException e) {
                Log.i(
                        SyncService.class.getSimpleName(), e.getMessage(), e
                );
            }
        }
    }

    private void sync(@NotNull String buildConfigurationId,
                      @NotNull String parentProjectId,
                      long bound) throws IOException {
        if (bound == DBUtils.UNDEFINED_TIME) {
            return;
        }

        HttpResponse response = myClient.getBuilds(buildConfigurationId, bound);
        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            throw new HttpStatusException(statusLine);
        } else {
            List<Build> builds = ParserPackage.parseBuilds(response.getEntity().getContent());

            if (!builds.isEmpty()) {
                myDB.appendBuilds(builds);

                notify(buildConfigurationId, parentProjectId, builds.size());
            }
        }
    }

    private void notify(@NotNull String buildConfigurationId,
                        @NotNull String parentProjectId,
                        int size) {
        Notification.Builder builder = new Notification.Builder(this);

        String title = size + " new build" + (size == 1 ? "" : "s");
        String projectName = myDB.getProjectName(parentProjectId);
        String buildConfigurationName = myDB.getBuildConfigurationName(buildConfigurationId);

        Intent activityIntent = new Intent(this, BuildConfigurationOverviewActivity.class);
        activityIntent.putExtra(BuildConfigurationOverviewActivity.ID_INTENT_KEY, buildConfigurationId);
        activityIntent.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                activityIntent,
                0
        );

        //noinspection deprecation
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(projectName + " - " + buildConfigurationName)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .getNotification();

        myManager.notify(buildConfigurationId.hashCode(), notification);
    }
}
