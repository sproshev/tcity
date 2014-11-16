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

package com.tcity.android.storage;

import com.tcity.android.concept.Project;
import com.tcity.android.parser.ParserPackage;
import com.tcity.android.rest.RestPackage;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

class ProjectsLoader implements Runnable {

    @NotNull
    private final Request<Collection<Project>> myRequest;

    ProjectsLoader(@NotNull Request<Collection<Project>> request) {
        myRequest = request;
    }

    @Override
    public void run() {
        if (myRequest.isCancelledOrExecuted()) {
            return;
        }

        try {
            HttpResponse response = RestPackage.loadProjects();
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                myRequest.sendException(
                        new IOException(
                                statusLine.getStatusCode() + " " + statusLine.getReasonPhrase()
                        )
                );
            } else {
                if (myRequest.isCancelledOrExecuted()) {
                    return;
                }

                myRequest.sendResult(
                        ParserPackage.parseProjects(
                                response.getEntity().getContent()
                        )
                );
            }
        } catch (IOException e) {
            myRequest.sendException(e);
        }
    }
}
