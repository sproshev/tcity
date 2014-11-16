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

import android.os.AsyncTask;

import com.tcity.android.Request;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Queue;

class StorageDriverQueueTask extends AsyncTask<Storage, Void, Void> {

    @NotNull
    private final Queue<Request<Collection<Project>>> myProjectsRequests;

    StorageDriverQueueTask(@NotNull Queue<Request<Collection<Project>>> projectsRequests) {
        myProjectsRequests = projectsRequests;
    }

    @Override
    protected Void doInBackground(@NotNull Storage... params) {
        Storage storage = params[0];

        while (!myProjectsRequests.isEmpty()) {
            storage.addProjectsRequest(myProjectsRequests.poll());
        }

        return null;
    }
}
