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

class ExecutableRunnableChain extends AsyncTask<Void, Exception, Void> implements RunnableChain.Listener {

    @NotNull
    private final RunnableChain myRunnableChain;

    @Nullable
    private final RunnableChain.Listener myListener;

    public ExecutableRunnableChain(@NotNull RunnableChain runnableChain,
                                   @Nullable RunnableChain.Listener listener) {
        myRunnableChain = runnableChain;
        myListener = listener;
    }

    @Override
    public void onException(@NotNull Exception e) {
        publishProgress(e);
    }

    @Override
    public void onFinished() {
        // NO-OP
    }

    @Override
    protected Void doInBackground(@NotNull Void... params) {
        myRunnableChain.run(this);

        return null;
    }

    @Override
    protected void onProgressUpdate(Exception... values) {
        if (myListener != null) {
            myListener.onException(values[0]);
        }
    }

    @Override
    protected void onPostExecute(@NotNull Void result) {
        if (myListener != null) {
            myListener.onFinished();
        }
    }
}
