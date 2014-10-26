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
import com.tcity.android.ui.ProjectsReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

class ProjectsLoaderRunnable implements Runnable {

    @NotNull
    private final ProjectsReceiver myReceiver;

    ProjectsLoaderRunnable(@NotNull ProjectsReceiver receiver) {
        myReceiver = receiver;
    }

    @Override
    public void run() {
        try {
            HttpResponse response = RestPackage.loadProjects();

            // TODO check receiver

            if (!isOk(response)) {
                myReceiver.receive(
                        new IOException(
                                calculateExceptionMessage(response)
                        )
                );
            } else {
                myReceiver.receive(
                        ParserPackage.parseProjects(
                                response.getEntity().getContent()
                        )
                );
            }
        } catch (IOException e) {
            myReceiver.receive(e);
        }
    }

    private boolean isOk(@NotNull HttpResponse response) {
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }

    @NotNull
    private String calculateExceptionMessage(@NotNull HttpResponse response) {
        return response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
    }
}
