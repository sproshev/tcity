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

package com.tcity.android.loader;

import android.os.AsyncTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RunnableChain implements Runnable {

    @NotNull
    public static RunnableChain getAndRunnableChain(@NotNull Runnable... runnables) {
        return new FirstLevelRunnableChain(true, runnables);
    }

    @NotNull
    public static RunnableChain getOrRunnableChain(@NotNull Runnable... runnables) {
        return new FirstLevelRunnableChain(false, runnables);
    }

    @NotNull
    public static RunnableChain getSingleRunnableChain(@NotNull Runnable runnable) {
        return new FirstLevelRunnableChain(true, runnable);
    }

    @NotNull
    public static RunnableChain getAndRunnableChain(@NotNull RunnableChain... runnableChains) {
        return new ElderLevelRunnableChain(true, runnableChains);
    }

    @NotNull
    public static RunnableChain getOrRunnableChain(@NotNull RunnableChain... runnableChains) {
        return new ElderLevelRunnableChain(false, runnableChains);
    }

    @Override
    public void run() {
        run(null);
    }

    @NotNull
    public AsyncTask<Void, Exception, Void> toAsyncTask(@Nullable Listener listener) {
        return new ExecutableRunnableChain(this, listener);
    }

    protected abstract boolean run(@Nullable Listener listener);

    public static interface Listener {

        public void onFinished();

        public void onException(@NotNull Exception e);
    }
}
