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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.tcity.android.R;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.Request;
import com.tcity.android.storage.StorageDriver;
import com.tcity.android.storage.TaskFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    @NotNull
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    @NotNull
    private List<Map<String, Object>> myData;

    @NotNull
    private MainActivityAdapter myAdapter;

    @Nullable
    private Request<Collection<Project>> myLastProjectsRequest;

    @NotNull
    private StorageDriver myStorageDriver;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myData = new ArrayList<>();
        myAdapter = new MainActivityAdapter(this, myData);
        myLastProjectsRequest = null;
        myStorageDriver = StorageDriver.getInstance(this);

        ListView overview = (ListView) findViewById(R.id.overview_list);
        overview.setAdapter(myAdapter);

        loadAllData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /* LIFECYCLE - END */

    private void loadAllData() {
        Log.d(LOG_TAG, "Loading has been started");

        loadProjects();

        // TODO
    }

    private void loadProjects() {
        if (myLastProjectsRequest != null) {
            myLastProjectsRequest.cancel();

            Log.d(
                    LOG_TAG,
                    "Previous projects request has been cancelled"
            );
        }

        myLastProjectsRequest = new Request<>(new ProjectsTaskFactory());

        myStorageDriver.addProjectsRequest(myLastProjectsRequest);

        Log.d(
                LOG_TAG,
                "Projects request has been sent"
        );
    }

    private class OnProjectsSuccessTask extends AsyncTask<Void, Void, Void> {

        @NotNull
        private final Collection<Project> myProjects;

        private OnProjectsSuccessTask(@NotNull Collection<Project> projects) {
            myProjects = projects;
        }

        @Override
        protected Void doInBackground(@NotNull Void... params) {
            Log.d(
                    LOG_TAG,
                    "Projects have been received: [size: " + myProjects.size() + "]"
            );

            myData.clear();

            Map<String, Object> separatorMap = new HashMap<>();

            separatorMap.put(MainActivityAdapter.SEPARATOR_TEXT_KEY, "Projects");
            separatorMap.put(MainActivityAdapter.TYPE_KEY, MainActivityAdapter.SEPARATOR_ITEM);

            myData.add(separatorMap);

            for (Project project : myProjects) {
                Map<String, Object> map = new HashMap<>();

                map.put(MainActivityAdapter.CONCEPT_FOLLOW_KEY, android.R.drawable.star_off); // TODO
                map.put(MainActivityAdapter.CONCEPT_NAME_KEY, project.getName());
                map.put(MainActivityAdapter.TYPE_KEY, MainActivityAdapter.CONCEPT_ITEM);

                myData.add(map);
            }

            return null;
        }

        @Override
        protected void onPostExecute(@NotNull Void aVoid) {
            myAdapter.notifyDataSetChanged();
        }
    }

    private class OnProjectsExceptionTask extends AsyncTask<Void, Void, Void> {

        @NotNull
        private final Exception myException;

        OnProjectsExceptionTask(@NotNull Exception e) {
            myException = e;
        }

        @Override
        protected Void doInBackground(@NotNull Void... params) {
            Log.w(LOG_TAG, myException.getMessage());

            return null;
        }
    }

    private class ProjectsTaskFactory implements TaskFactory<Collection<Project>> {
        @Nullable
        @Override
        public AsyncTask<Void, Void, Void> createOnSuccessTask(@NotNull Collection<Project> projects) {
            return new OnProjectsSuccessTask(projects);
        }

        @Nullable
        @Override
        public AsyncTask<Void, Void, Void> createOnExceptionTask(@NotNull Exception e) {
            return new OnProjectsExceptionTask(e);
        }
    }
}
