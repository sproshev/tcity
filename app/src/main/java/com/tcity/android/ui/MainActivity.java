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

import java.util.List;

public class MainActivity extends Activity implements ProjectsReceiver, View.OnClickListener {

    @NotNull
    private ServiceConnection myConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button_main);
        button.setOnClickListener(this);

        myConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                ((DataService.Binder) binder).getService().loadProjects(MainActivity.this);
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };
    }

    @Override
    public void receive(@NotNull List<Project> projects) {
        Log.d("TAG", Integer.toString(projects.size()));
        unbindService(myConnection);
    }

    @Override
    public void receive(@NotNull Exception e) {
        Log.d("TAG", e.getMessage());
        unbindService(myConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(myConnection);
    }

    @Override
    public void onClick(@NotNull View v) {
        bindService(
                new Intent(this, DataService.class),
                myConnection,
                BIND_AUTO_CREATE
        );
    }
}
