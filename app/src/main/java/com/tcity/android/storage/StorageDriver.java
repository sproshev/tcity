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

package com.tcity.android.storage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.tcity.android.Request;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO discuss

public class StorageDriver {

    @Nullable
    private static StorageDriver INSTANCE;

    @NotNull
    private final Context myContext;

    @NotNull
    private final ServiceConnection myConnection;

    @NotNull
    private final ConcurrentLinkedQueue<Request<Collection<Project>>> myProjectsRequests;

    @NotNull
    private StorageDriverQueueTask myQueueTask;

    @Nullable
    private Storage myStorage;

    private StorageDriver(@NotNull Context context) {
        myContext = context.getApplicationContext();
        myConnection = new ServiceConnection();

        myProjectsRequests = new ConcurrentLinkedQueue<>();

        myQueueTask = new StorageDriverQueueTask(myProjectsRequests);
    }

    @NotNull
    public static StorageDriver getInstance(@NotNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new StorageDriver(context);
        }

        return INSTANCE;
    }

    public void addProjectsRequest(@NotNull Request<Collection<Project>> request) {
        if (myStorage != null) {
            myStorage.addProjectsRequest(request);
        } else {
            myProjectsRequests.add(request);

            bindService();
        }
    }

    public void close() {
        myQueueTask.cancel(true);
        myStorage = null;
        myContext.unbindService(myConnection);
    }

    private void bindService() {
        myContext.bindService(
                new Intent(myContext, Storage.class),
                myConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    private void runQueueTask() {
        AsyncTask.Status status = myQueueTask.getStatus();

        if (status.equals(AsyncTask.Status.RUNNING)) {
            return;
        }

        if (status.equals(AsyncTask.Status.FINISHED) || myQueueTask.isCancelled()) {
            myQueueTask = new StorageDriverQueueTask(myProjectsRequests);
        }

        myQueueTask.execute(myStorage);
    }

    private class ServiceConnection implements android.content.ServiceConnection {

        // TODO WeakReference?

        public void onServiceConnected(ComponentName name, IBinder binder) {
            myStorage = ((Storage.Binder) binder).getService();

            runQueueTask();
        }

        public void onServiceDisconnected(ComponentName name) {
            myStorage = null;
        }
    }
}
