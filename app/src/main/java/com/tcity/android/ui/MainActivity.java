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
import android.widget.SimpleAdapter;

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
    private static final String FOLLOW_KEY = "follow";

    @NotNull
    private static final String NAME_KEY = "name";

    @NotNull
    private static final String[] FROM = new String[]{FOLLOW_KEY, NAME_KEY};

    @NotNull
    private static final int[] TO = new int[]{R.id.imagebutton_follow, R.id.list_item_name};

    @NotNull
    private static final String PR_LOG_TAG = ProjectsRequest.class.getCanonicalName();

    @NotNull
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    @NotNull
    private MainStorage myStorage;

    private int myLastProjectsRequestId;

    @NotNull
    private List<Map<String, Object>> myAllProjects;

    @NotNull
    private SimpleAdapter myAllProjectsAdapter;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAllProjects = new ArrayList<>();
        myAllProjectsAdapter = new SimpleAdapter(this, myAllProjects, R.layout.list_item_concept, FROM, TO);
        myStorage = MainStorage.getInstance(this);

        ListView allProjectsListView = (ListView) findViewById(R.id.listview_all_projects);
        allProjectsListView.setAdapter(myAllProjectsAdapter);

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
                myAllProjects.clear();

                for (Project project : projects) {
                    Map<String, Object> map = new HashMap<>();

                    map.put(FOLLOW_KEY, android.R.drawable.star_off); // TODO
                    map.put(NAME_KEY, project.getName());

                    myAllProjects.add(map);
                }

                // TODO discuss

                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                myAllProjectsAdapter.notifyDataSetChanged();
                            }
                        }
                );
            }
        }

        @Override
        public void receive(@NotNull Exception e) {
            if (myId == myLastProjectsRequestId) {
                // TODO
            }
        }
    }
}
