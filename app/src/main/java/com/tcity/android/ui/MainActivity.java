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
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;

import com.tcity.android.R;
import com.tcity.android.concept.Project;
import com.tcity.android.service.DataService;
import com.tcity.android.service.ProjectsRequest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MainActivity extends ListActivity implements ProjectsRequest, View.OnClickListener {

    @NotNull
    private ServiceConnection myConnection;

    @Nullable
    private DataService myDataService = null;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] values = new String[]
                {"A", "B", "C", "D", "E", "F"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.listitem_project,
                R.id.listitem_project_name,
                values
        );

        setListAdapter(adapter);

        myConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                myDataService = ((DataService.Binder) binder).getService();
            }

            public void onServiceDisconnected(ComponentName name) {
                myDataService = null;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService(
                new Intent(this, DataService.class),
                myConnection,
                BIND_AUTO_CREATE
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (myDataService != null) {
            unbindService(myConnection);
            myDataService = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /* LIFECYCLE - END */

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void receive(@NotNull Collection<Project> projects) {
        Log.d("TAG", Integer.toString(projects.size()));
    }

    @Override
    public void receive(@NotNull Exception e) {
        Log.d("TAG", e.getMessage());
    }

    @Override
    public void onClick(@NotNull View v) {
        if (myDataService != null) {
            myDataService.addProjectsRequest(this);
        }
    }
}
