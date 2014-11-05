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
import android.view.View;
import android.widget.Button;

import com.tcity.android.R;
import com.tcity.android.concept.Project;
import com.tcity.android.storage.MainStorage;
import com.tcity.android.storage.Request;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MainActivity extends Activity implements View.OnClickListener {

    @NotNull
    private MainStorage myStorage;

    private int myLastProjectsRequestId = Integer.MIN_VALUE;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /*
        String[] values = new String[]
                {"A", "B", "C", "D", "E", "F"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.listitem_project,
                R.id.listitem_project_name,
                values
        );

        setListAdapter(adapter);
        */

        Button button = (Button) findViewById(R.id.button_main);
        button.setOnClickListener(this);

        myStorage = MainStorage.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /* LIFECYCLE - END */

    @Override
    public void onClick(@NotNull View v) {
        myLastProjectsRequestId++;
        myStorage.addProjectsRequest(new ProjectsRequest(myLastProjectsRequestId));
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
        public void receive(Collection<? extends Project> projects) {
            if (myId == myLastProjectsRequestId) {
                Log.d("TAG", Integer.toString(projects.size()));
            }
        }

        @Override
        public void receive(@NotNull Exception e) {
            if (myId == myLastProjectsRequestId) {
                Log.d("TAG", e.getMessage());
            }
        }
    }
}
