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

package com.tcity.android.storage.driver;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.tcity.android.storage.Binder;
import com.tcity.android.storage.Storage;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

class StorageConnection implements ServiceConnection {

    @NotNull
    private final WeakReference<StorageDriver> myDriverWeakReference;

    StorageConnection(@NotNull StorageDriver driver) {
        myDriverWeakReference = new WeakReference<>(driver);
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
        StorageDriver driver = myDriverWeakReference.get();

        if (driver != null) {
            Storage storage = ((Binder) binder).getService();

            driver.setStorage(storage);
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        StorageDriver driver = myDriverWeakReference.get();

        if (driver != null) {
            driver.setStorage(null);
        }
    }
}
