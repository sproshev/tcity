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

import com.tcity.android.Receiver;
import com.tcity.android.Request;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Queue;

class ProjectsReceiver extends Receiver<Collection<Project>> {

    @NotNull
    private final WeakReference<Storage> myStorageWeakReference;

    ProjectsReceiver(@NotNull Storage storage) {
        super(storage.getLooper());

        myStorageWeakReference = new WeakReference<>(storage);
    }

    @Override
    public void handleResult(@NotNull Collection<Project> projects) {
        Storage storage = myStorageWeakReference.get();

        if (storage != null) {
            storage.setProjectsCache(projects);
            Queue<Request<Collection<Project>>> projectsRequests = storage.getProjectsRequests();

            while (!projectsRequests.isEmpty()) {
                projectsRequests.poll().sendResult(projects);
            }
        }
    }

    @Override
    public void handleException(@NotNull Exception e) {
        Storage storage = myStorageWeakReference.get();

        if (storage != null) {
            Queue<Request<Collection<Project>>> projectsRequests = storage.getProjectsRequests();

            while (!projectsRequests.isEmpty()) {
                projectsRequests.poll().sendException(e);
            }
        }
    }
}
