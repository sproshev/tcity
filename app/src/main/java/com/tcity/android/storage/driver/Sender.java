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

import android.os.AsyncTask;

import com.tcity.android.Request;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.Storage;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Queue;

class Sender extends AsyncTask<Void, Void, Void> {

    @NotNull
    private final WeakReference<StorageDriver> myDriverWeakReference;

    Sender(@NotNull StorageDriver driver) {
        myDriverWeakReference = new WeakReference<>(driver);
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        StorageDriver driver = myDriverWeakReference.get();
        Storage storage = driver == null ? null : driver.getStorage();

        if (storage != null) {
            Queue<Request<Collection<Project>>> projectsRequests = driver.getProjectsRequests();

            while (!projectsRequests.isEmpty()) {
                storage.addProjectsRequest(projectsRequests.poll());
            }
        }

        return null;
    }
}
