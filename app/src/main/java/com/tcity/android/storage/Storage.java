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

import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Storage extends Service {

    @NotNull
    private ExecutorService myExecutorService;

    @NotNull
    private Binder myBinder;

    @NotNull
    private AtomicBoolean myProjectsLoaderSubmitted;

    @NotNull
    private ConcurrentLinkedQueue<Request<Collection<Project>>> myProjectsRequests;

    @Nullable
    private Collection<Project> myProjectsCache;

    @NotNull
    private ProjectsReceiver myProjectsReceiver;

    /* LIFECYCLE - BEGIN */

    @Override
    public void onCreate() {
        super.onCreate();
        myExecutorService = Executors.newSingleThreadExecutor();
        myBinder = new Binder();

        myProjectsLoaderSubmitted = new AtomicBoolean();
        myProjectsRequests = new ConcurrentLinkedQueue<>();
        myProjectsCache = null;
        myProjectsReceiver = new ProjectsReceiver();
    }

    @Override
    public IBinder onBind(@NotNull Intent intent) {
        return myBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myExecutorService.shutdown();
    }

    @Override
    public void onTrimMemory(int level) {
        Iterator<Request<Collection<Project>>> projectsRequestIterator = myProjectsRequests.iterator();

        while (projectsRequestIterator.hasNext()) {
            if (projectsRequestIterator.next().isCancelledOrReceived()) {
                projectsRequestIterator.remove();
            }
        }

        if (myProjectsRequests.isEmpty()) {
            myProjectsCache = null;
        }
    }

    /* LIFECYCLE - END */

    public void addProjectsRequest(@NotNull Request<Collection<Project>> request) {
        if (myProjectsLoaderSubmitted.get()) {
            myProjectsRequests.add(request);
            return;
        }

        if (myProjectsCache != null) {
            request.receive(myProjectsCache);
        } else {
            myProjectsRequests.add(request);

            myProjectsLoaderSubmitted.set(true);
            myExecutorService.submit(new ProjectsLoader(myProjectsReceiver));
        }
    }

    public class Binder extends android.os.Binder {

        @NotNull
        public Storage getService() {
            return Storage.this;
        }
    }

    private class ProjectsReceiver implements Receiver<Collection<Project>> {

        @Override
        public void receive(@NotNull Collection<Project> projects) {
            myProjectsCache = projects;
            myProjectsLoaderSubmitted.set(false);

            while (!myProjectsRequests.isEmpty()) {
                myProjectsRequests.poll().receive(projects);
            }
        }

        @Override
        public void receive(@NotNull Exception e) {
            myProjectsLoaderSubmitted.set(false);

            while (!myProjectsRequests.isEmpty()) {
                myProjectsRequests.poll().receive(e);
            }
        }
    }

}
