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

import com.tcity.android.R;
import com.tcity.android.Request;
import com.tcity.android.Settings;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.driver.StorageDriver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends ListActivity implements OverviewListener {

    @NotNull
    private Settings mySettings;

    @NotNull
    private OverviewAdapter myAdapter;

    @Nullable
    private Request<Collection<Project>> myLastProjectsRequest;

    @NotNull
    private StorageDriver myStorageDriver;

    @NotNull
    private final Set<String> myWatchedProjectIds = new HashSet<>();

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mySettings = Settings.getInstance(this);

        myWatchedProjectIds.addAll(mySettings.getWatchedProjectIds());

        myAdapter = new OverviewAdapter(
                this,
                this,
                getResources().getString(R.string.projects),
                getResources().getString(R.string.build_configurations),
                getResources().getString(R.string.builds)
        );
        myAdapter.updateWatchedProjectIds(myWatchedProjectIds);

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

    @Override
    public void onChangeProjectWatch(@NotNull String id) {
        if (myWatchedProjectIds.contains(id)) {
            myWatchedProjectIds.remove(id);
        } else {
            myWatchedProjectIds.add(id);
        }

        myAdapter.updateWatchedProjectIds(myWatchedProjectIds);
        mySettings.setWatchedProjectIds(myWatchedProjectIds);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChangeBuildConfigurationWatch(@NotNull String id) {
        // TODO
    }

    @Override
    public void onChangeBuildWatch(@NotNull String id) {
        // TODO
    }

    @Override
    public void onProjectOptionsClick(@NotNull String id) {
        // TODO
    }

    @Override
    public void onBuildConfigurationOptionsClick(@NotNull String id) {
        // TODO
    }

    @Override
    public void onBuildOptionsClick(@NotNull String id) {
        // TODO
    }

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
