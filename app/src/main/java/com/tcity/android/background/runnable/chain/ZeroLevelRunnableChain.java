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

package com.tcity.android.background.runnable.chain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ZeroLevelRunnableChain extends RunnableChain {

    private final boolean myStopOnException;

    @NotNull
    private final Runnable[] myRunnables;

    ZeroLevelRunnableChain(boolean stopOnException, @NotNull Runnable... runnables) {
        myStopOnException = stopOnException;
        myRunnables = runnables;
    }

    @Override
    protected boolean run(@Nullable Listener listener) {
        for (Runnable runnable : myRunnables) {
            try {
                runnable.run();
            } catch (Exception e) {
                if (listener != null) {
                    listener.onException(e);
                }

                if (myStopOnException) {
                    return false;
                }
            }
        }

        return true;
    }
}
