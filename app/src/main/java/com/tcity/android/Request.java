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

package com.tcity.android;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class Request<T> {

    @NotNull
    private final AtomicBoolean myIsValid = new AtomicBoolean(true);

    @NotNull
    private final WeakReference<Receiver<T>> myReceiverWeakReference;

    public Request(@NotNull Receiver<T> receiver) {
        myReceiverWeakReference = new WeakReference<>(receiver);
    }

    public void sendResult(@NotNull T data) {
        if (myIsValid.get()) {
            myIsValid.set(false);

            Receiver<T> receiver = myReceiverWeakReference.get();

            if (receiver != null) {
                receiver.receiveResult(data);
            }
        }
    }

    public void sendException(@NotNull Exception e) {
        if (myIsValid.get()) {
            myIsValid.set(false);

            Receiver<T> receiver = myReceiverWeakReference.get();

            if (receiver != null) {
                receiver.receiveException(e);
            }
        }
    }

    public void invalidate() {
        myIsValid.set(false);
    }

    public boolean isValid() {
        if (myReceiverWeakReference.get() == null) {
            invalidate();
        }

        return myIsValid.get();
    }
}
