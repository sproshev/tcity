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

package com.tcity.android.ui.info;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import com.tcity.android.app.Application;
import com.tcity.android.db.DB;

import org.jetbrains.annotations.NotNull;

public class BuildHostActivity extends Activity implements ActionBar.TabListener {

    @NotNull
    public static final String ID_INTENT_KEY = "BUILD_ID";

    @NotNull
    private String myBuildId;

    // LIFECYCLE - Begin

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myBuildId = getIntent().getStringExtra(ID_INTENT_KEY);

        ActionBar bar = getActionBar();
        assert bar != null;

        bar.setTitle(calculateTitle());
        bar.setSubtitle(calculateSubtitle());
        bar.setDisplayHomeAsUpEnabled(true);

        /*
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab tab = bar.newTab();
        tab.setText("Info");
        tab.setTabListener(this);
        bar.addTab(tab);

        tab = bar.newTab();
        tab.setText("Tests");
        tab.setTabListener(this);
        bar.addTab(tab);

        tab = bar.newTab();
        tab.setText("Artifacts");
        tab.setTabListener(this);
        bar.addTab(tab);
        */
    }

    // LIFECYCLE - End

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @NotNull
    private String calculateTitle() {
        DB db = ((Application) getApplication()).getDB();

        return db.getBuildName(myBuildId);
    }

    @NotNull
    private String calculateSubtitle() {
        DB db = ((Application) getApplication()).getDB();

        return db.getBuildConfigurationName(db.getBuildParentId(myBuildId));
    }
}
