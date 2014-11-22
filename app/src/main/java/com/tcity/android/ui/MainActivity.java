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

import android.app.ListActivity;
import android.os.Bundle;

import com.tcity.android.Request;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.driver.StorageDriver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class MainActivity extends ListActivity {

    @NotNull
    private OverviewAdapter myAdapter;

    @Nullable
    private Request<Collection<Project>> myLastProjectsRequest;

    @NotNull
    private StorageDriver myStorageDriver;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myAdapter = new OverviewAdapter(
                this,
                "Projects",
                null,
                null,
                Collections.<String>emptySet(),
                Collections.<String>emptySet(),
                Collections.<String>emptySet()
        );

        myLastProjectsRequest = null;
        myStorageDriver = StorageDriver.getInstance(this);

        getListView().setAdapter(myAdapter);

        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myLastProjectsRequest != null) {
            myLastProjectsRequest.invalidate();
        }

        if (isTaskRoot()) {
            myStorageDriver.close();
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
                            this, myAdapter
                    )
            );

            myStorageDriver.addProjectsRequest(myLastProjectsRequest);
        }
    }
}
