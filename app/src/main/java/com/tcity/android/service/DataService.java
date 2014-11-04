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

package com.tcity.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataService extends Service {

    @NotNull
    private ExecutorService myExecutorService;

    @NotNull
    private Binder myBinder;

    @NotNull
    private SparseArray<ProjectsRequestExecutor> myExecutors;

    /* LIFECYCLE - BEGIN */

    @Override
    public void onCreate() {
        super.onCreate();
        myExecutorService = Executors.newSingleThreadExecutor();
        myBinder = new Binder();
        myExecutors = new SparseArray<>();
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

    /* LIFECYCLE - END */

    public void addProjectsRequest(@NotNull ProjectsRequest request) {
        ProjectsRequestExecutor executor = new ProjectsRequestExecutor(request);

        myExecutors.put(getProjectsRequestKey(request), executor);
        myExecutorService.submit(executor);
    }

    public void removeProjectsRequest(@NotNull ProjectsRequest request) {
        int key = getProjectsRequestKey(request);

        myExecutors.get(key).terminate();
        myExecutors.remove(key);
    }

    private int getProjectsRequestKey(@NotNull ProjectsRequest request) {
        return request.getId();
    }

    public class Binder extends android.os.Binder {

        @NotNull
        public DataService getService() {
            return DataService.this;
        }
    }
}
