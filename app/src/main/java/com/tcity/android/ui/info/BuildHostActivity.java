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
import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.db.DB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildHostActivity extends Activity {

    @NotNull
    public static final String ID_INTENT_KEY = "BUILD_ID";

    @NotNull
    private static final String SELECTED_TAB_KEY = "SELECTED_TAB";

    @NotNull
    private String myBuildId;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myBuildId = getIntent().getStringExtra(ID_INTENT_KEY);

        initActionBar(
                savedInstanceState != null ? savedInstanceState.getInt(SELECTED_TAB_KEY, 0) : 0
        );
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //noinspection deprecation,ConstantConditions
        outState.putInt(SELECTED_TAB_KEY, getActionBar().getSelectedTab().getPosition());
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

    @SuppressWarnings("deprecation")
    private void initActionBar(int selectedTab) {
        ActionBar bar = getActionBar();

        assert bar != null;

        bar.setTitle(calculateTitle());
        bar.setSubtitle(calculateSubtitle());
        bar.setDisplayHomeAsUpEnabled(true);

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        initTab(bar, getString(R.string.info), BuildInfoFragment.class);
        initTab(bar, getString(R.string.tests), BuildTestsFragment.class);

        bar.setSelectedNavigationItem(selectedTab);
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

    @SuppressWarnings("deprecation")
    private <T extends Fragment> void initTab(@NotNull ActionBar bar,
                                              @NotNull String name,
                                              @NotNull Class<T> cls) {
        ActionBar.Tab tab = bar.newTab();

        tab.setText(name);
        tab.setTabListener(
                new BuildTabListener<>(
                        this,
                        cls,
                        name,
                        myBuildId
                )
        );

        bar.addTab(tab);
    }
}
