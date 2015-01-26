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

package com.tcity.android.background.rest;

import com.tcity.android.app.Preferences;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

public class RestClient {

    private static final int TIMEOUT = 5000;

    @NotNull
    private final HttpClient myHttpClient;

    @NotNull
    private final Preferences myPreferences;

    public RestClient(@NotNull Preferences preferences) {
        myHttpClient = new DefaultHttpClient(calculateHttpParams());
        myPreferences = preferences;
    }

    @NotNull
    public HttpResponse getRoot() throws IOException {
        return getPlain(
                RestLocator.getRoot(myPreferences)
        );
    }

    @NotNull
    public HttpResponse getOverviewProjects(@NotNull String login) throws IOException {
        return getPlain(
                RestLocator.getOverviewProjects(login, myPreferences)
        );
    }

    @NotNull
    public HttpResponse getProjectId(@NotNull String internalId) throws IOException {
        return getPlain(
                RestLocator.getProjectId(internalId, myPreferences)
        );
    }

    @NotNull
    public HttpResponse getProjects() throws IOException {
        return getJson(
                RestLocator.getProjectsUrl(myPreferences)
        );
    }

    @NotNull
    public HttpResponse getBuildConfigurations(@NotNull String projectId) throws IOException {
        return getJson(
                RestLocator.getBuildConfigurationsUrl(projectId, myPreferences)
        );
    }

    @NotNull
    public HttpResponse getBuilds(@NotNull String buildConfigurationId) throws IOException {
        return getJson(
                RestLocator.getBuildsUrl(buildConfigurationId, myPreferences)
        );
    }

    @NotNull
    public HttpResponse getBuilds(@NotNull String buildConfigurationId,
                                  long sinceMillis) throws IOException {
        return getJson(
                RestLocator.getBuildsUrl(buildConfigurationId, sinceMillis, myPreferences)
        );
    }

    @NotNull
    public HttpResponse getProjectStatus(@NotNull String id) throws IOException {
        return getPlain(
                RestLocator.getProjectStatusUrl(id, myPreferences)
        );
    }

    @NotNull
    public HttpResponse getBuildConfigurationStatus(@NotNull String id) throws IOException {
        return getPlain(
                RestLocator.getBuildConfigurationStatusUrl(id, myPreferences)
        );
    }

    @NotNull
    private HttpResponse getJson(@NotNull String path) throws IOException {
        return get(path, "application/json");
    }

    @NotNull
    private HttpResponse getPlain(@NotNull String path) throws IOException {
        return get(path, "text/plain");
    }

    @NotNull
    private HttpResponse get(@NotNull String path, @NotNull String format) throws IOException {
        HttpRequestBase request = new HttpGet();

        request.addHeader("Authorization", "Basic " + myPreferences.getAuth());
        request.addHeader("Accept", format);

        request.setURI(URI.create(path));

        return myHttpClient.execute(request);
    }

    @NotNull
    private HttpParams calculateHttpParams() {
        HttpParams httpParams = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);

        return httpParams;
    }
}
