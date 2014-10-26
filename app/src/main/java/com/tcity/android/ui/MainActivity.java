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
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tcity.android.R;
import com.tcity.android.concept.Project;
import com.tcity.android.service.DataService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MainActivity extends Activity implements ProjectsReceiver, View.OnClickListener {

    @NotNull
    private ServiceConnection myConnection;

    @Nullable
    private DataService myDataService = null;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button_main);
        button.setOnClickListener(this);

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

    /* LIFECYCLE - END */

    @Override
    public void receive(@NotNull List<Project> projects) {
        Log.d("TAG", Integer.toString(projects.size()));
    }

    @Override
    public void receive(@NotNull Exception e) {
        Log.d("TAG", e.getMessage());
    }

    @Override
    public void onClick(@NotNull View v) {
        if (myDataService != null) {
            myDataService.loadProjects(this);
        }
    }
}
