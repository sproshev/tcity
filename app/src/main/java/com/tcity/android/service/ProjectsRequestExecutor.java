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

package com.tcity.android.service;

import com.tcity.android.parser.ParserPackage;
import com.tcity.android.rest.RestPackage;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

class ProjectsRequestExecutor implements Runnable {

    @NotNull
    private final ProjectsRequest myRequest;

    private boolean isTerminated = false;

    ProjectsRequestExecutor(@NotNull ProjectsRequest request) {
        myRequest = request;
    }

    @Override
    public void run() {
        if (!isTerminated) {
            return;
        }

        try {
            HttpResponse response = RestPackage.loadProjects();

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                checkAndSendException(
                        new IOException(
                                calculateExceptionMessage(response)
                        )
                );
            } else {
                checkAndSendProjects(response);
            }
        } catch (IOException e) {
            checkAndSendException(e);
        }
    }

    public void terminate() {
        isTerminated = true;
    }

    private void checkAndSendException(@NotNull Exception e) {
        if (!isTerminated) {
            myRequest.receive(e);
        }
    }

    private void checkAndSendProjects(@NotNull HttpResponse response) throws IOException {
        if (!isTerminated) {
            myRequest.receive(
                    ParserPackage.parseProjects(
                            response.getEntity().getContent()
                    )
            );
        }
    }

    @NotNull
    private String calculateExceptionMessage(@NotNull HttpResponse response) {
        return response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
    }
}
