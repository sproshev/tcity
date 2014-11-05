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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

class RemoteStorageRequestExecutor<T> implements Runnable {

    @NotNull
    private final Request<T> myRequest;

    @NotNull
    private final HttpLoader myLoader;

    @NotNull
    private final Parser<T> myParser;

    private boolean isTerminated = false;

    RemoteStorageRequestExecutor(@NotNull Request<T> request,
                                 @NotNull HttpLoader loader,
                                 @NotNull Parser<T> parser) {
        myRequest = request;
        myLoader = loader;
        myParser = parser;
    }

    @Override
    public void run() {
        if (!isTerminated) {
            try {
                HttpResponse response = myLoader.load();

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
                    myParser.parse(
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
