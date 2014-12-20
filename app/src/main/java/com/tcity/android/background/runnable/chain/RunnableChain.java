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

public abstract class RunnableChain {

    @NotNull
    public static RunnableChain getAndRunnableChain(@NotNull Runnable... runnables) {
        return new ZeroLevelRunnableChain(true, runnables);
    }

    @NotNull
    public static RunnableChain getOrRunnableChain(@NotNull Runnable... runnables) {
        return new ZeroLevelRunnableChain(false, runnables);
    }

    @NotNull
    public static RunnableChain getSingleRunnableChain(@NotNull Runnable runnable) {
        return new ZeroLevelRunnableChain(true, runnable);
    }

    @NotNull
    public static RunnableChain getAndRunnableChain(@NotNull RunnableChain... chains) {
        return new DeeperLevelRunnableChain(true, chains);
    }

    @NotNull
    public static RunnableChain getOrRunnableChain(@NotNull RunnableChain... chains) {
        return new DeeperLevelRunnableChain(false, chains);
    }

    @NotNull
    public ExecutableRunnableChain toAsyncTask(@Nullable Listener listener) {
        return new ExecutableRunnableChain(this, listener);
    }

    protected abstract boolean run(@Nullable Listener listener);

    public static interface Listener {

        public void onFinished();

        public void onException(@NotNull Exception e);
    }
}
