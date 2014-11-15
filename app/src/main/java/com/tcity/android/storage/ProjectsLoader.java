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
    private final Receiver<Collection<Project>> myReceiver;

    ProjectsLoader(@NotNull Receiver<Collection<Project>> receiver) {
        myReceiver = receiver;
    }

    @Override
    public void run() {
        try {
            HttpResponse response = RestPackage.loadProjects();
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                myReceiver.receive(
                        new IOException(
                                statusLine.getStatusCode() + " " + statusLine.getReasonPhrase()
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
}
