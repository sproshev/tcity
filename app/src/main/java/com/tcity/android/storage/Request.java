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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class Request<T> {

    @NotNull
    private final TaskFactory<T> myTaskFactory;

    @NotNull
    private final AtomicBoolean myIsInvalid = new AtomicBoolean();

    @Nullable
    private AsyncTask<Void, Void, Void> myOnSuccessTask = null;

    @Nullable
    private AsyncTask<Void, Void, Void> myOnExceptionTask = null;

    public Request(@NotNull TaskFactory<T> taskFactory) {
        myTaskFactory = taskFactory;
    }

    public void receive(@NotNull T data) {
        if (!myIsInvalid.get()) {
            myIsInvalid.set(true);
            myOnSuccessTask = myTaskFactory.createOnSuccessTask(data);

            if (myOnSuccessTask != null) {
                myOnSuccessTask.execute();
            }
        }
    }

    public void receive(@NotNull Exception e) {
        if (!myIsInvalid.get()) {
            myIsInvalid.set(true);
            myOnExceptionTask = myTaskFactory.createOnExceptionTask(e);

            if (myOnExceptionTask != null) {
                myOnExceptionTask.execute();
            }
        }
    }

    public void cancel() {
        myIsInvalid.set(true);

        if (myOnSuccessTask != null) {
            myOnSuccessTask.cancel(true);
        }

        if (myOnExceptionTask != null) {
            myOnExceptionTask.cancel(true);
        }
    }
}
