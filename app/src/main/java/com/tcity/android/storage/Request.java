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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class Request<T> {

    @NotNull
    private final AtomicBoolean myIsCancelledOrExecuted = new AtomicBoolean();

    @Nullable
    private final Receiver<T> myReceiver;

    public Request(@Nullable Receiver<T> receiver) {
        myReceiver = receiver;
    }

    public void sendResult(@NotNull T data) {
        if (!myIsCancelledOrExecuted.get()) {
            myIsCancelledOrExecuted.set(true);

            if (myReceiver != null) {
                myReceiver.receiveResult(data);
            }
        }
    }

    public void sendException(@NotNull Exception e) {
        if (!myIsCancelledOrExecuted.get()) {
            myIsCancelledOrExecuted.set(true);

            if (myReceiver != null) {
                myReceiver.receiveException(e);
            }
        }
    }

    public void cancel() {
        myIsCancelledOrExecuted.set(true);
    }

    public boolean isCancelledOrExecuted() {
        return myIsCancelledOrExecuted.get();
    }
}
