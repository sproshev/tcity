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

package com.tcity.android.loader

import android.os.AsyncTask

public fun getOrRunnablesChain(
        runnables: Collection<Runnable>,
        listener: ChainListener? = null
): RunnablesChain = RunnablesChain(runnables, false, listener)

public fun getAndRunnablesChain(
        runnables: Collection<Runnable>,
        listener: ChainListener? = null
): RunnablesChain = RunnablesChain(runnables, true, listener)

public trait ChainListener {

    public fun onFinished()

    public fun onException(e: Exception)
}

private class RunnablesChain(
        private val runnables: Collection<Runnable>,
        private val stopOnException: Boolean,
        private val listener: ChainListener?
) : AsyncTask<Void, Exception, Void>() {

    override fun doInBackground(vararg params: Void?): Void? {
        for (runnable in runnables) {
            try {
                runnable.run()
            } catch (e: Exception) {
                publishProgress(e)

                if (stopOnException) {
                    break
                }
            }
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Exception) {
        listener?.onException(values[0])
    }

    override fun onPostExecute(result: Void?) {
        listener?.onFinished()
    }
}
