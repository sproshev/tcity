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

package com.tcity.android.storage

import java.io.InputStream
import java.io.IOException
import org.apache.http.HttpResponse
import com.tcity.android.concept.Project

public trait Request<T> {

    public fun getId(): Int

    public fun receive(t: T)

    public fun receive(e: Exception)
}

public trait Storage {

    public fun addProjectsRequest(request: Request<Collection<Project>>)

    public fun removeProjectsRequest(request: Request<Collection<Project>>)
}

trait Parser<T> {

    throws(javaClass<IOException>())
    public fun parse(stream: InputStream): T
}

trait HttpLoader {

    throws(javaClass<IOException>())
    public fun load(): HttpResponse
}
