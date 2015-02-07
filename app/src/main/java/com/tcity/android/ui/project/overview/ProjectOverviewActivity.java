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

package com.tcity.android.ui.project.overview;

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
import com.tcity.android.obj.Project;
import com.tcity.android.ui.buildconfiguration.overview.BuildConfigurationOverviewActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectOverviewActivity
        extends ListActivity
        implements SwipeRefreshLayout.OnRefreshListener, ProjectOverviewListener {

    @NotNull
    public static final String ID_INTENT_KEY = "PROJECT_ID";

    @NotNull
    private String myProjectId;

    @NotNull
    private SwipeRefreshLayout myLayout;

    @NotNull
    private ProjectOverviewEngine myEngine;

    private boolean myRecreating;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myRecreating = false;
        myProjectId = calculateProjectId();

        setContentView(R.layout.overview_ui);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(calculateTitle());
            bar.setSubtitle(calculateSubtitle());

            if (!isRootProject(myProjectId)) {
                bar.setDisplayHomeAsUpEnabled(true);
            }
        }

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

        myEngine.setListener(this);
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

        myEngine.setListener(null);
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
        myProjectId = null;
    }

    // LIFECYCLE - End

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home && !isRootProject(myProjectId)) {
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        myEngine.refresh(true);
    }

    @Override
    public void onRefreshRunning() {
        setRefreshing(true);
    }

    @Override
    public void onRefreshFinished() {
        setRefreshing(false);
    }

    @Override
    public void onRefreshException() {
        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = myEngine.getException();

        if (e != null) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

            myEngine.resetException();
        }
    }

    @Override
    public void projectImageClick(@NotNull String id) {
        myEngine.projectImageClick(id);
    }

    @Override
    public void projectDescriptionClick(@NotNull String id) {
        Intent intent = new Intent(this, ProjectOverviewActivity.class);
        intent.putExtra(ID_INTENT_KEY, id);

        startActivity(intent);
    }

    @Override
    public void projectOptionsClick(@NotNull String id, @NotNull View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.inflate(R.menu.menu_concept);

        menu.setOnMenuItemClickListener(
                new Common.PopupMenuListener(
                        this,
                        WebLocator.getProjectUrl(id, new Preferences(this))
                )
        );

        menu.show();
    }

    @Override
    public void buildConfigurationImageClick(@NotNull String id) {
        myEngine.buildConfigurationImageClick(id);
    }

    @Override
    public void buildConfigurationDescriptionClick(@NotNull String id) {
        Intent intent = new Intent(this, BuildConfigurationOverviewActivity.class);
        intent.putExtra(BuildConfigurationOverviewActivity.ID_INTENT_KEY, id);

        startActivity(intent);
    }

    @Override
    public void buildConfigurationOptionsClick(@NotNull String id, @NotNull View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.inflate(R.menu.menu_concept);

        menu.setOnMenuItemClickListener(
                new Common.PopupMenuListener(
                        this,
                        WebLocator.getBuildConfigurationUrl(id, new Preferences(this))
                )
        );

        menu.show();
    }

    @NotNull
    private String calculateProjectId() {
        if (getIntent().hasExtra(ID_INTENT_KEY)) {
            return getIntent().getStringExtra(ID_INTENT_KEY);
        } else {
            return Project.ROOT_PROJECT_ID;
        }
    }

    @NotNull
    private String calculateTitle() {
        if (isRootProject(myProjectId)) {
            return getString(R.string.projects);
        }

        DB db = ((Application) getApplication()).getDb();

        return db.getProjectName(myProjectId);
    }

    @Nullable
    private String calculateSubtitle() {
        if (isRootProject(myProjectId)) {
            return null;
        }

        DB db = ((Application) getApplication()).getDb();

        String parentId = db.getProjectParentId(myProjectId);

        if (isRootProject(parentId)) {
            return null;
        }

        return db.getProjectName(parentId);
    }

    private boolean isRootProject(@NotNull String id) {
        return id.equals(Project.ROOT_PROJECT_ID);
    }

    @NotNull
    private ProjectOverviewEngine calculateEngine() {
        //noinspection deprecation
        ProjectOverviewEngine result = (ProjectOverviewEngine) getLastNonConfigurationInstance();

        if (result == null) {
            result = new ProjectOverviewEngine(
                    myProjectId,
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
