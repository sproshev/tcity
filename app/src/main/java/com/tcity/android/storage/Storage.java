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

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import com.tcity.android.Request;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Storage extends Service {

    @NotNull
    private HandlerThread myBackgroundThread;

    @NotNull
    private Handler myBackgroundHandler;

    @NotNull
    private StorageBinder myBinder;

    @Nullable
    private Request<Collection<Project>> myProjectsLoaderRequest;

    @Nullable
    private Collection<Project> myProjectsCache;

    @NotNull
    private ConcurrentLinkedQueue<Request<Collection<Project>>> myProjectsRequests;

    @NotNull
    private StorageProjectsReceiver myProjectsReceiver;

    /* LIFECYCLE - BEGIN */

    @Override
    public void onCreate() {
        super.onCreate();
        myBackgroundThread = new HandlerThread("StorageBackground", Process.THREAD_PRIORITY_BACKGROUND);
        myBackgroundThread.start();
        myBackgroundHandler = new Handler(myBackgroundThread.getLooper());

        myBinder = new StorageBinder(this);

        myProjectsLoaderRequest = null;
        myProjectsCache = null;
        myProjectsRequests = new ConcurrentLinkedQueue<>();
        myProjectsReceiver = new StorageProjectsReceiver(this);
    }

    @Override
    public IBinder onBind(@NotNull Intent intent) {
        return myBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (myProjectsLoaderRequest != null) {
            myProjectsLoaderRequest.invalidate();
        }

        myBackgroundThread.quit();
    }

    @Override
    public void onTrimMemory(int level) {
        trimProjects();
    }

    /* LIFECYCLE - END */

    public void addProjectsRequest(@NotNull Request<Collection<Project>> request) {
        if (myProjectsLoaderRequest != null && myProjectsLoaderRequest.isValid()) {
            myProjectsRequests.add(request);
            return;
        }

        if (myProjectsCache != null) {
            request.sendResult(myProjectsCache);
        } else {
            myProjectsRequests.add(request);

            myProjectsLoaderRequest = new Request<>(myProjectsReceiver);
            myBackgroundHandler.post(new LoadProjectsRunnable(myProjectsLoaderRequest));
        }
    }

    @NotNull
    Looper getLooper() {
        return myBackgroundHandler.getLooper();
    }

    void setProjectsCache(@Nullable Collection<Project> projects) {
        myProjectsCache = projects;
    }

    @NotNull
    Queue<Request<Collection<Project>>> getProjectsRequests() {
        return myProjectsRequests;
    }

    private void trimProjects() {
        Iterator<Request<Collection<Project>>> iterator = myProjectsRequests.iterator();

        while (iterator.hasNext()) {
            if (!iterator.next().isValid()) {
                iterator.remove();
            }
        }

        if (myProjectsRequests.isEmpty()) {
            myProjectsCache = null;
        }
    }
}
