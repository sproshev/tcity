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

package com.tcity.android.ui;

import android.app.Activity;
import android.os.Bundle;

import com.tcity.android.Request;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.StorageDriver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MainActivity extends Activity {

    @NotNull
    private OverviewCalculator myOverviewCalculator;

    @Nullable
    private Request<Collection<Project>> myLastProjectsRequest;

    @NotNull
    private StorageDriver myStorageDriver;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myOverviewCalculator = new OverviewCalculator(this);
        myLastProjectsRequest = null;
        myStorageDriver = StorageDriver.getInstance(this);

        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myLastProjectsRequest != null) {
            myLastProjectsRequest.invalidate();
        }
    }

    /* LIFECYCLE - END */

    private void loadData() {
        loadProjects();
    }

    private void loadProjects() {
        if (myLastProjectsRequest == null || !myLastProjectsRequest.isValid()) {
            myLastProjectsRequest = new Request<>(
                    new ProjectsReceiver(
                            new ViewReceiver(this), myOverviewCalculator
                    )
            );

            myStorageDriver.addProjectsRequest(myLastProjectsRequest);
        }
    }
}
