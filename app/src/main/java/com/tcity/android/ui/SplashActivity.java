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
import android.content.Intent;
import android.os.Bundle;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;
import com.tcity.android.ui.overview.project.ProjectOverviewActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!new Preferences(this).isValid()) {
            // startActivity(new Intent(this, LoginActivity.class)); TODO
        } else {
            startActivity(new Intent(this, ProjectOverviewActivity.class));
        }

        finish();
    }
}
