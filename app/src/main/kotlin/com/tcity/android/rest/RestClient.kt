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
import android.util.Base64

private val restPath = "/httpAuth/app/rest/"

private val connectionTimeout = 5000
private val httpClient = DefaultHttpClient(calculateHttpParams())

[throws(javaClass<IOException>())]
public fun loadProjects(): HttpResponse {
    return load("projects")
}

[throws(javaClass<IOException>())]
public fun load(path: String): HttpResponse {
    val request = HttpGet()

    request.addHeader("Authorization", "Basic " + calculateEncodedCredentials())
    request.addHeader("Accept", "application/json")
    request.setURI(URI.create("host" + restPath + path))

    return httpClient.execute(request)
}

private fun calculateEncodedCredentials(): String {
    return Base64.encodeToString("user:pass".toByteArray(), Base64.NO_WRAP)
}

private fun calculateHttpParams(): HttpParams {
    val httpParams = BasicHttpParams()

    HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout)
    HttpConnectionParams.setSoTimeout(httpParams, connectionTimeout)

    return httpParams
}
