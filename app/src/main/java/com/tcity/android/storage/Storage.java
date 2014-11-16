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
import android.os.IBinder;

import com.tcity.android.Receiver;
import com.tcity.android.Request;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Storage extends Service {

    @NotNull
    private ExecutorService myExecutorService;

    @NotNull
    private Binder myBinder;

    @Nullable
    private Request<Collection<Project>> myProjectsLoaderRequest;

    @Nullable
    private Collection<Project> myProjectsCache;

    @NotNull
    private ConcurrentLinkedQueue<Request<Collection<Project>>> myProjectsRequests;

    /* LIFECYCLE - BEGIN */

    @Override
    public void onCreate() {
        super.onCreate();
        myExecutorService = Executors.newSingleThreadExecutor();
        myBinder = new Binder();

        myProjectsLoaderRequest = null;
        myProjectsCache = null;
        myProjectsRequests = new ConcurrentLinkedQueue<>();
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

        myExecutorService.shutdown();
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

            myProjectsLoaderRequest = new Request<>(new ProjectsReceiver());
            myExecutorService.submit(new ProjectsLoader(myProjectsLoaderRequest));
        }
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

    public class Binder extends android.os.Binder {

        // TODO WeakReference?

        @NotNull
        public Storage getService() {
            return Storage.this;
        }
    }

    private class ProjectsReceiver extends Receiver<Collection<Project>> {

        // TODO WeakReference?

        @Override
        public void handleResult(@NotNull Collection<Project> projects) {
            myProjectsCache = projects;

            while (!myProjectsRequests.isEmpty()) {
                myProjectsRequests.poll().sendResult(projects); // TODO maybe make async
            }
        }

        @Override
        public void handleException(@NotNull Exception e) {
            while (!myProjectsRequests.isEmpty()) {
                myProjectsRequests.poll().sendException(e); // TODO maybe make async
            }
        }
    }

}
