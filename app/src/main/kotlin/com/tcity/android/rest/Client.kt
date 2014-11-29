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

package com.tcity.android.rest

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpParams
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpResponse
import java.net.URI
import java.io.IOException

private val CONNECTION_TIMEOUT = 5000
private val HTTP_CLIENT = DefaultHttpClient(calculateHttpParams())

[throws(javaClass<IOException>())]
public fun get(path: String, auth: String): HttpResponse {
    val request = HttpGet()

    request.addHeader("Authorization", "Basic " + auth)
    request.addHeader("Accept", "application/json")
    request.setURI(URI.create(path))

    return HTTP_CLIENT.execute(request)
}

private fun calculateHttpParams(): HttpParams {
    val httpParams = BasicHttpParams()

    HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT)
    HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIMEOUT)

    return httpParams
}
