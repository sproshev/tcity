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

import android.os.Handler;
import android.os.Message;

import org.jetbrains.annotations.NotNull;

public abstract class Receiver<T> extends Handler {

    public static final int RESULT = 0;
    public static final int EXCEPTION = 1;

    @Override
    public abstract void handleMessage(@NotNull Message msg);

    public void receiveResult(@NotNull T t) {
        sendMessage(
                obtainMessage(RESULT, t)
        );
    }

    public void receiveException(@NotNull Exception e) {
        sendMessage(
                obtainMessage(EXCEPTION, e)
        );
    }
}
