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

package com.tcity.android.app;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Application extends android.app.Application {

    @Nullable
    private Preferences myPreferences;

    @Nullable
    private DB myDB;

    @NotNull
    public Preferences getPreferences() {
        if (myPreferences == null) {
            myPreferences = new Preferences(this);
        }

        return myPreferences;
    }

    @NotNull
    public DB getDB() {
        if (myDB == null) {
            myDB = new DB(this);
        }

        return myDB;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (myPreferences != null) {
            myPreferences.onTrimMemory();
        }
    }
}
