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

package com.tcity.android.ui.overview.buildconfiguration;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
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
import com.tcity.android.ui.info.BuildHostActivity;
import com.tcity.android.ui.preference.PreferenceActivity;

import org.jetbrains.annotations.NotNull;

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
    protected void onCreate(Bundle savedInstanceState) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.menu_settings);
        item.setIntent(new Intent(this, PreferenceActivity.class));

        return super.onCreateOptionsMenu(menu);
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

    void nameClick(@NotNull String id) {
        Intent intent = new Intent(this, BuildHostActivity.class);
        intent.putExtra(BuildHostActivity.ID_INTENT_KEY, id);

        startActivity(intent);
    }

    void optionsClick(@NotNull String id, @NotNull View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.inflate(R.menu.menu_concept);

        menu.setOnMenuItemClickListener(
                new PopupMenuListener(
                        WebLocator.getBuildUrl(id, new Preferences(this))
                )
        );

        menu.show();
    }

    @NotNull
    private String calculateTitle() {
        DB db = ((Application) getApplication()).getDB();

        return db.getBuildConfigurationName(myBuildConfigurationId);
    }

    @NotNull
    private String calculateSubtitle() {
        DB db = ((Application) getApplication()).getDB();

        return db.getProjectName(db.getBuildConfigurationParentId(myBuildConfigurationId));
    }

    private void updateSyncBound() {
        DB db = ((Application) getApplication()).getDB();
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
                    ((Application) getApplication()).getDB(),
                    getListView()
            );
        }

        return result;
    }

    private void setRefreshing(final boolean refreshing) {
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (myLayout.isRefreshing() ^ refreshing) {
                            myLayout.setRefreshing(refreshing);

                            TextView emptyView = (TextView) getListView().getEmptyView();

                            if (refreshing) {
                                emptyView.setText(R.string.loading);
                            } else {
                                if (Common.isNetworkAvailable(BuildConfigurationOverviewActivity.this)) {
                                    emptyView.setText(R.string.empty);
                                } else {
                                    emptyView.setText(R.string.network_is_unavailable);
                                }
                            }
                        }
                    }
                }, 500
        );  // https://code.google.com/p/android/issues/detail?id=77712
    }

    private class PopupMenuListener implements PopupMenu.OnMenuItemClickListener {

        @NotNull
        private final String myUrl;

        private PopupMenuListener(@NotNull String url) {
            myUrl = url;
        }

        @Override
        public boolean onMenuItemClick(@NotNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_share_link:
                    onShareClick();

                    return true;
                case R.id.menu_open_in_browser:
                    onOpenClick();

                    return true;
                default:
                    return false;
            }
        }

        private void onShareClick() {
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    myUrl
            );

            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_link)));
        }

        private void onOpenClick() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(myUrl));

            startActivity(intent);
        }
    }
}
