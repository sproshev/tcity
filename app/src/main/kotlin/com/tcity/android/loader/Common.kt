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

import org.apache.http.StatusLine
import java.io.IOException
import android.os.Handler
import java.util.concurrent.atomic.AtomicBoolean

private fun executeSafety(lambda: () -> Unit, receiver: Receiver? = null) {
    try {
        lambda()

        receiver?.receiveSuccess()
    } catch (e: Exception) {
        receiver?.receiveException(e)
    }
}

public class HttpStatusException(status: StatusLine) : IOException("${status.getStatusCode()} ${status.getReasonPhrase()}")

public class Receiver(private val handler: Handler) {

    class object {
        public val ERROR_CODE: Int = -1
        public val OK_CODE: Int = 0
    }

    private var disabled = AtomicBoolean(false)

    public fun disable() {
        disabled.set(true)
    }

    public fun receiveSuccess() {
        if (!disabled.get()) {
            handler.sendEmptyMessage(OK_CODE)
        }
    }

    public fun receiveException(e: Exception) {
        if (!disabled.get()) {
            handler.sendMessage(
                    handler.obtainMessage(ERROR_CODE, e)
            )
        }
    }
}