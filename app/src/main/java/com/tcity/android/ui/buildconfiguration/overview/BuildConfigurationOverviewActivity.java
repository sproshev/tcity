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

package com.tcity.android.ui.buildconfiguration.overview;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.Common;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.web.WebLocator;
import com.tcity.android.db.DB;
import com.tcity.android.db.DBUtils;
import com.tcity.android.ui.build.BuildActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildConfigurationOverviewActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    public static final String ID_INTENT_KEY = "BUILD_CONFIGURATION_ID";

    @NotNull
    private String myBuildConfigurationId;

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private BuildConfigurationOverviewEngine myEngine;

    private boolean myRecreating;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myRecreating = false;
        myBuildConfigurationId = getIntent().getStringExtra(ID_INTENT_KEY);

        setContentView(R.layout.overview_ui);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(calculateTitle());
            bar.setSubtitle(calculateSubtitle());
            bar.setDisplayHomeAsUpEnabled(true);
        }

        updateSyncBound();

        myLayout = (SwipeRefreshLayout) findViewById(R.id.overview_srlayout);
        myLayout.setColorSchemeResources(R.color.green, R.color.red);
        myLayout.setOnRefreshListener(this);

        myEngine = calculateEngine();
        setListAdapter(myEngine.getAdapter());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Common.isNetworkAvailable(this)) {
            myEngine.refresh(false);
        } else {
            ((TextView) getListView().getEmptyView()).setText(R.string.network_is_unavailable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (myEngine.isRefreshing()) {
            onRefreshRunning();
        } else {
            onRefreshException();
            onRefreshFinished();
        }

        myEngine.setActivity(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object onRetainNonConfigurationInstance() {
        myRecreating = true;

        return myEngine;
    }

    @Override
    protected void onPause() {
        super.onPause();

        myEngine.setActivity(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!myRecreating) {
            myEngine.close();

            //noinspection ConstantConditions
            myEngine = null;
        }

        //noinspection ConstantConditions
        myLayout = null;

        //noinspection ConstantConditions
        myBuildConfigurationId = null;
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
    public void onRefresh() {
        myEngine.refresh(true);
    }

    void onRefreshRunning() {
        setRefreshing(true);
    }

    void onRefreshFinished() {
        setRefreshing(false);

        //noinspection ThrowableResultOfMethodCallIgnored
        if (myEngine.getException() == null) {
            updateSyncBound();
        }

        myEngine.resetException();
    }

    void onRefreshException() {
        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = myEngine.getException();

        if (e != null) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void imageClick(@NotNull String id) {
        myEngine.imageClick(id);
    }

    void descriptionClick(@NotNull String id) {
        Intent intent = new Intent(this, BuildActivity.class);
        intent.putExtra(BuildActivity.ID_INTENT_KEY, id);

        startActivity(intent);
    }

    void optionsClick(@NotNull String id, @NotNull View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.inflate(R.menu.menu_concept);

        menu.setOnMenuItemClickListener(
                new Common.PopupMenuListener(
                        this,
                        WebLocator.getBuildUrl(id, new Preferences(this))
                )
        );

        menu.show();
    }

    @NotNull
    private String calculateTitle() {
        DB db = ((Application) getApplication()).getDb();

        return db.getBuildConfigurationName(myBuildConfigurationId);
    }

    @NotNull
    private String calculateSubtitle() {
        DB db = ((Application) getApplication()).getDb();

        return db.getProjectName(db.getBuildConfigurationParentId(myBuildConfigurationId));
    }

    private void updateSyncBound() {
        DB db = ((Application) getApplication()).getDb();
        long lastUpdate = db.getBuildConfigurationLastUpdate(myBuildConfigurationId);

        if (db.isBuildConfigurationFavourite(myBuildConfigurationId) && lastUpdate != DBUtils.UNDEFINED_TIME) {
            db.setBuildConfigurationSyncBound(
                    myBuildConfigurationId,
                    lastUpdate
            );
        }
    }

    @NotNull
    private BuildConfigurationOverviewEngine calculateEngine() {
        //noinspection deprecation
        BuildConfigurationOverviewEngine result = (BuildConfigurationOverviewEngine) getLastNonConfigurationInstance();

        if (result == null) {
            result = new BuildConfigurationOverviewEngine(
                    myBuildConfigurationId,
                    this,
                    ((Application) getApplication()).getDb(),
                    getListView()
            );
        }

        return result;
    }

    private void setRefreshing(boolean refreshing) {
        Common.setRefreshing(
                this,
                myLayout,
                (TextView) getListView().getEmptyView(),
                refreshing
        );
    }
}