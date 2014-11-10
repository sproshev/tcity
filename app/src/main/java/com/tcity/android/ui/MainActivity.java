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
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.tcity.android.R;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.MainStorage;
import com.tcity.android.storage.Request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    @NotNull
    private static final String PR_LOG_TAG = ProjectsRequest.class.getCanonicalName();

    @NotNull
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    @NotNull
    private MainStorage myStorage;

    private int myLastProjectsRequestId;

    @NotNull
    private List<Map<String, Object>> myData;

    @NotNull
    private MainActivityAdapter myAdapter;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myData = new ArrayList<>();
        myAdapter = new MainActivityAdapter(this, myData);
        myStorage = MainStorage.getInstance(this);

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
        myLastProjectsRequestId = myStorage.createId();

        myStorage.addProjectsRequest(new ProjectsRequest(myLastProjectsRequestId));

        Log.d(
                LOG_TAG,
                "Projects loading has been started: [id: " + myLastProjectsRequestId + "]"
        );
    }

    private class ProjectsRequest implements Request<Collection<? extends Project>> {

        private final int myId;

        private ProjectsRequest(int id) {
            myId = id;
        }

        @Override
        public int getId() {
            return myId;
        }

        @Override
        public void receive(@NotNull Collection<? extends Project> projects) {
            Log.d(
                    PR_LOG_TAG,
                    "Projects have been received: [" +
                            "id: " + myId +
                            ", " +
                            "lastId: " + myLastProjectsRequestId +
                            ", " +
                            "size: " + projects.size() +
                            "]"
            );

            if (myId == myLastProjectsRequestId) {
                myData.clear();

                Map<String, Object> projectsSeparatorMap = new HashMap<>();
                projectsSeparatorMap.put(MainActivityAdapter.SEPARATOR_TEXT_KEY, "Projects");
                projectsSeparatorMap.put(MainActivityAdapter.TYPE_KEY, MainActivityAdapter.SEPARATOR_ITEM);
                myData.add(projectsSeparatorMap);

                for (Project project : projects) {
                    Map<String, Object> map = new HashMap<>();

                    map.put(MainActivityAdapter.CONCEPT_FOLLOW_KEY, android.R.drawable.star_off); // TODO
                    map.put(MainActivityAdapter.CONCEPT_NAME_KEY, project.getName());
                    map.put(MainActivityAdapter.TYPE_KEY, MainActivityAdapter.CONCEPT_ITEM);

                    myData.add(map);
                }

                // TODO discuss

                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                );
            }
        }

        @Override
        public void receive(@NotNull Exception e) {
            if (myId == myLastProjectsRequestId) {
                Log.w(PR_LOG_TAG, e.getMessage());

                // TODO
            }
        }
    }
}
