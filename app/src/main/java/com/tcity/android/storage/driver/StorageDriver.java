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

package com.tcity.android.storage.driver;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.tcity.android.Request;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.Storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StorageDriver {

    @Nullable
    private static StorageDriver INSTANCE;

    @NotNull
    private final Context myContext;

    @NotNull
    private final Connection myConnection;

    @NotNull
    private final ConcurrentLinkedQueue<Request<Collection<Project>>> myProjectsRequests;

    @NotNull
    private Sender mySender;

    @Nullable
    private Storage myStorage = null;

    private StorageDriver(@NotNull Context context) {
        myContext = context.getApplicationContext();
        myConnection = new Connection(this);

        myProjectsRequests = new ConcurrentLinkedQueue<>();

        mySender = new Sender(this);
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
        mySender.cancel(true);
        myStorage = null;
        myContext.unbindService(myConnection);
    }

    @Nullable
    Storage getStorage() {
        return myStorage;
    }

    void setStorage(@Nullable Storage storage) {
        myStorage = storage;

        if (myStorage != null) {
            executeSender();
        }
    }

    @NotNull
    Queue<Request<Collection<Project>>> getProjectsRequests() {
        return myProjectsRequests;
    }

    private void bindService() {
        myContext.bindService(
                new Intent(myContext, Storage.class),
                myConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    private void executeSender() {
        AsyncTask.Status status = mySender.getStatus();

        if (status.equals(AsyncTask.Status.RUNNING)) {
            return;
        }

        if (status.equals(AsyncTask.Status.FINISHED) || mySender.isCancelled()) {
            mySender = new Sender(this);
        }

        mySender.execute();
    }
}
