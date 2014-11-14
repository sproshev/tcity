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

public class Request<T> {

    @NotNull
    private final AsyncTask<T, Void, Void> myOnSuccessTask;

    @NotNull
    private final AsyncTask<Exception, Void, Void> myOnExceptionTask;

    private boolean myIsCancelled = false;

    public Request(@NotNull AsyncTask<T, Void, Void> onSuccessTask,
                   @NotNull AsyncTask<Exception, Void, Void> onExceptionTask) {
        myOnSuccessTask = onSuccessTask;
        myOnExceptionTask = onExceptionTask;
    }

    public void receive(@NotNull T data) {
        if (!myIsCancelled) {
            myOnSuccessTask.execute(data);
        }
    }

    public void receive(@NotNull Exception e) {
        if (!myIsCancelled) {
            myOnExceptionTask.execute(e);
        }
    }

    public void cancel() {
        myIsCancelled = true;
        myOnSuccessTask.cancel(true);
        myOnExceptionTask.cancel(true);
    }
}
