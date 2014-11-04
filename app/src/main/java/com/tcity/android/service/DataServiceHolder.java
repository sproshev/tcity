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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.SparseArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataServiceHolder {

    @Nullable
    private static DataServiceHolder INSTANCE;

    @NotNull
    private final Context myContext;

    @NotNull
    private final SparseArray<DataServiceRequest> myRequests = new SparseArray<>();

    @NotNull
    private final ServiceConnection myConnection;

    @Nullable
    private DataService myService;

    private DataServiceHolder(@NotNull Context context) {
        myContext = context.getApplicationContext();

        myConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                myService = ((DataService.Binder) binder).getService();

                executeAllRequests();
            }

            public void onServiceDisconnected(ComponentName name) {
                myService = null;
            }
        };
    }

    @NotNull
    public static DataServiceHolder getInstance(@NotNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DataServiceHolder(context);
        }

        return INSTANCE;
    }

    public void addRequest(@NotNull DataServiceRequest request) {
        if (myService != null) {
            request.receive(myService);
        } else {
            myContext.bindService(
                    new Intent(myContext, DataService.class),
                    myConnection,
                    Context.BIND_AUTO_CREATE
            );

            myRequests.put(getRequestKey(request), request);
        }
    }

    public void removeRequest(@NotNull DataServiceRequest request) {
        myRequests.remove(getRequestKey(request));
    }

    public void onTrimMemory() {
        myService = null;
        myContext.unbindService(myConnection);
    }

    private void executeAllRequests() {
        while (myRequests.size() != 0 && myService != null) {
            for (int index = 0; index < myRequests.size(); index++) {
                if (!executeRequest(myRequests.valueAt(index))) {
                    break;
                }
            }
        }
    }

    private boolean executeRequest(@NotNull DataServiceRequest request) {
        if (myService != null) {
            request.receive(myService);
            myRequests.remove(getRequestKey(request));

            return true;
        } else {
            return false;
        }
    }

    private int getRequestKey(@NotNull DataServiceRequest request) {
        return request.getId();
    }
}
