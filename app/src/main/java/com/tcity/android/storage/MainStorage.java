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
import android.os.IBinder;
import android.util.SparseArray;

import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class MainStorage implements Storage {

    @Nullable
    private static MainStorage INSTANCE;

    @NotNull
    private final AtomicInteger myLastId = new AtomicInteger(-1);

    @NotNull
    private final Context myContext;

    @NotNull
    private final ServiceConnection myConnection;

    @NotNull
    private final SparseArray<RequestTransmitter<?>> myTransmitters = new SparseArray<>();

    @Nullable
    private RemoteStorage myRemoteStorage;

    @NotNull
    private final Callable<Collection<? extends Project>> myProjectsCallable = new Callable<Collection<? extends Project>>() {
        @Override
        public void call(@NotNull RemoteStorage remoteStorage,
                         @NotNull Request<Collection<? extends Project>> request) {
            remoteStorage.addProjectsRequest(request);
        }
    };

    private MainStorage(@NotNull Context context) {
        myContext = context.getApplicationContext();
        myConnection = new ServiceConnection();
    }

    @NotNull
    public static MainStorage getInstance(@NotNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MainStorage(context);
        }

        return INSTANCE;
    }

    public int createId() {
        return myLastId.addAndGet(1);
    }

    @Override
    public void addProjectsRequest(@NotNull Request<Collection<? extends Project>> request) {
        addRequest(request, myProjectsCallable);
    }

    @Override
    public void removeProjectsRequest(@NotNull Request<Collection<? extends Project>> request) {
        if (myRemoteStorage != null) {
            myRemoteStorage.removeProjectsRequest(request);
        } else {
            myTransmitters.remove(getRequestKey(request));
        }
    }

    private <T> void addRequest(@NotNull Request<T> request, @NotNull Callable<T> callable) {
        if (myRemoteStorage != null) {
            callable.call(myRemoteStorage, request);
        } else {
            myTransmitters.put(
                    getRequestKey(request),
                    new RequestTransmitter<>(request, callable)
            );

            myContext.bindService(
                    new Intent(myContext, RemoteStorage.class),
                    myConnection,
                    Context.BIND_AUTO_CREATE
            );
        }
    }

    public void onTrimMemory() {
        myRemoteStorage = null;
        myContext.unbindService(myConnection);
    }

    private void sendAllRequests() {
        while (myTransmitters.size() != 0 && myRemoteStorage != null) {
            for (int index = 0; index < myTransmitters.size(); index++) {
                RequestTransmitter<?> transmitter = myTransmitters.valueAt(index);

                if (!transmitter.transmit()) {
                    break;
                }
            }
        }
    }

    private int getRequestKey(@NotNull Request<?> request) {
        return request.getId();
    }

    private class ServiceConnection implements android.content.ServiceConnection {

        public void onServiceConnected(ComponentName name, IBinder binder) {
            myRemoteStorage = ((RemoteStorage.Binder) binder).getService();

            sendAllRequests();
        }

        public void onServiceDisconnected(ComponentName name) {
            myRemoteStorage = null;
        }
    }

    private class RequestTransmitter<T> {

        @NotNull
        private final Request<T> myRequest;

        @NotNull
        private final Callable<T> myCallable;

        private RequestTransmitter(@NotNull Request<T> request, @NotNull Callable<T> callable) {
            myRequest = request;
            myCallable = callable;
        }

        public boolean transmit() {
            if (myRemoteStorage != null) {
                myCallable.call(myRemoteStorage, myRequest);
                myTransmitters.remove(getRequestKey(myRequest));

                return true;
            } else {
                return false;
            }
        }
    }

    private static interface Callable<T> {

        public void call(@NotNull RemoteStorage remoteStorage, @NotNull Request<T> request);
    }
}
