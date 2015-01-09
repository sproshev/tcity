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
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.tcity.android.app.Preferences;
import com.tcity.android.background.web.WebLocator;
import com.tcity.android.db.DB;
import com.tcity.android.ui.PreferenceActivity;

import org.jetbrains.annotations.NotNull;

public class BuildConfigurationOverviewActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    public static final String INTENT_KEY = "BUILD_CONFIGURATION_ID";

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
        myBuildConfigurationId = getIntent().getStringExtra(INTENT_KEY);

        setContentView(R.layout.overview);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(calculateTitle());
        }

        myLayout = (SwipeRefreshLayout) findViewById(R.id.overview);
        myLayout.setColorSchemeResources(R.color.green, R.color.red);
        myLayout.setOnRefreshListener(this);

        myEngine = calculateEngine();
        setListAdapter(myEngine.getAdapter());

        if (isNetworkAvailable()) {
            myEngine.refresh(false);
        } else {
            ((TextView) getListView().getEmptyView()).setText(R.string.network_is_unavailable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (myEngine.isRefreshing()) {
            setRefreshing(true);
        } else {
            setRefreshing(false);

            //noinspection ThrowableResultOfMethodCallIgnored
            Exception e = myEngine.getException();

            if (e != null) {
                myEngine.resetException();

                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
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
        }
    }

    // LIFECYCLE - End

    @Override
    public void onRefresh() {
        myEngine.refresh(true);
    }

    void setRefreshing(final boolean refreshing) {
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
                                if (isNetworkAvailable()) {
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

    void imageClick(@NotNull String id) {
        myEngine.imageClick(id);
    }

    void nameClick(@NotNull String id) {
        // TODO
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

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
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
