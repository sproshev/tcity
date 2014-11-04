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

import com.tcity.android.concept.Project;
import com.tcity.android.parser.ParserPackage;
import com.tcity.android.rest.RestPackage;

import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataService extends Service {

    @NotNull
    private ExecutorService myExecutorService;

    @NotNull
    private Binder myBinder;

    @NotNull
    private SparseArray<DataRequestExecutor<?>> myExecutors;

    @NotNull
    private DataLoader myProjectsLoader = new DataLoader() {
        @NotNull
        @Override
        public HttpResponse load() throws IOException {
            return RestPackage.loadProjects();
        }
    };

    @NotNull
    private DataParser<Project> myProjectsParser = new DataParser<Project>() {
        @NotNull
        @Override
        public Collection<Project> parse(@NotNull InputStream stream) throws IOException {
            return ParserPackage.parseProjects(stream);
        }
    };

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

    public void addProjectsRequest(@NotNull DataRequest<Project> request) {
        DataRequestExecutor<Project> executor = new DataRequestExecutor<>(request, myProjectsLoader, myProjectsParser);

        myExecutors.put(getRequestKey(request), executor);
        myExecutorService.submit(executor);
    }

    public void removeProjectsRequest(@NotNull DataRequest<Project> request) {
        int key = getRequestKey(request);

        myExecutors.get(key).terminate();
        myExecutors.remove(key);
    }

    private int getRequestKey(@NotNull Request<?> request) {
        return request.getId();
    }

    public class Binder extends android.os.Binder {

        @NotNull
        public DataService getService() {
            return DataService.this;
        }
    }
}
