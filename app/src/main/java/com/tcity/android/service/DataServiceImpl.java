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

import com.tcity.android.parser.ParserPackage;
import com.tcity.android.rest.RestPackage;
import com.tcity.android.ui.ProjectsReceiver;

import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataServiceImpl extends Service implements DataService {

    @NotNull
    private final ExecutorService myExecutorService = Executors.newSingleThreadExecutor();

    @NotNull
    private final Binder myBinder = new Binder();

    @Override
    public IBinder onBind(@NotNull Intent intent) {
        return myBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myExecutorService.shutdown();
    }

    public void requestProjects(@NotNull ProjectsReceiver receiver) {
        myExecutorService.submit(new LoadProjectsTask(receiver));
    }

    public class Binder extends android.os.Binder {

        @NotNull
        public DataService getService() {
            return DataServiceImpl.this;
        }
    }

    private static class LoadProjectsTask implements Runnable {

        @NotNull
        private final ProjectsReceiver myReceiver;

        private LoadProjectsTask(@NotNull ProjectsReceiver receiver) {
            myReceiver = receiver;
        }

        @Override
        public void run() {
            try {
                HttpResponse httpResponse = RestPackage.executeGet(RestPackage.getProjectsUrl());

                // TODO check status
                // TODO check receiver

                myReceiver.receive(
                        ParserPackage.parseProjects(
                                httpResponse.getEntity().getContent()
                        )
                );
            } catch (IOException e) {
                // TODO check receiver

                myReceiver.receive(e);
            }
        }
    }
}
