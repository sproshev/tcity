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

package com.tcity.android.ui.build;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.web.WebLocator;
import com.tcity.android.db.DB;
import com.tcity.android.ui.build.artifacts.BuildArtifactsFragment;
import com.tcity.android.ui.build.info.BuildInfoFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BuildActivity extends Activity {

    @NotNull
    public static final String ID_INTENT_KEY = "BUILD_ID";

    @NotNull
    private static final String SELECTED_TAB_KEY = "SELECTED_TAB";

    @NotNull
    private String myBuildId;

    @NotNull
    private List<BuildTabListener<?>> myTabListeners;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myBuildId = getIntent().getStringExtra(ID_INTENT_KEY);
        myTabListeners = new ArrayList<>();

        initActionBar(
                savedInstanceState != null ? savedInstanceState.getInt(SELECTED_TAB_KEY, 0) : 0
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_build_host, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_TAB_KEY, getSelectedPosition());
    }

    // LIFECYCLE - End

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

            return true;
        }

        if (item.getItemId() == R.id.menu_dl_log) {
            //noinspection ResultOfMethodCallIgnored
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            manager.enqueue(calculateLogRequest());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (myTabListeners.get(getSelectedPosition()).onBackPressed()) {
            return;
        }

        super.onBackPressed();
    }

    private int getSelectedPosition() {
        //noinspection deprecation,ConstantConditions
        return getActionBar().getSelectedTab().getPosition();
    }

    @SuppressWarnings("deprecation")
    private void initActionBar(int selectedTab) {
        ActionBar bar = getActionBar();

        assert bar != null;

        bar.setTitle(calculateTitle());
        bar.setSubtitle(calculateSubtitle());
        bar.setDisplayHomeAsUpEnabled(true);

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        initTab(bar, getString(R.string.info), BuildInfoFragment.class, BuildInfoFragment.TAG);
        initTab(bar, getString(R.string.artifacts), BuildArtifactsFragment.class, BuildArtifactsFragment.TAG);

        bar.setSelectedNavigationItem(selectedTab);
    }

    @NotNull
    private DownloadManager.Request calculateLogRequest() {
        Preferences preferences = new Preferences(this);

        Uri src = Uri.parse(WebLocator.getBuildLogUrl(myBuildId, preferences));
        String dest = calculateLogFilename(preferences);

        DownloadManager.Request request = new DownloadManager.Request(src);

        request.addRequestHeader("Authorization", "Basic " + preferences.getAuth());
        request.setTitle(dest);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                dest
        );

        return request;
    }

    @NotNull
    private String calculateTitle() {
        DB db = ((Application) getApplication()).getDb();

        return db.getBuildName(myBuildId);
    }

    @NotNull
    private String calculateSubtitle() {
        DB db = ((Application) getApplication()).getDb();

        return db.getBuildConfigurationName(db.getBuildParentId(myBuildId));
    }

    @SuppressWarnings("deprecation")
    private <T extends Fragment> void initTab(@NotNull ActionBar bar,
                                              @NotNull String name,
                                              @NotNull Class<T> cls,
                                              @NotNull String tag) {
        ActionBar.Tab tab = bar.newTab();
        BuildTabListener<T> listener = new BuildTabListener<>(
                this,
                cls,
                tag,
                myBuildId
        );

        tab.setText(name);
        tab.setTabListener(
                listener
        );

        bar.addTab(tab);
        myTabListeners.add(listener);
    }

    @NotNull
    private String calculateLogFilename(@NotNull Preferences preferences) {
        DB db = ((Application) getApplication()).getDb();

        String buildConfigurationId = db.getBuildParentId(myBuildId);
        String projectId = db.getBuildConfigurationParentId(buildConfigurationId);

        String projectName = db.getProjectName(projectId);
        String buildConfigurationName = db.getBuildConfigurationName(buildConfigurationId);
        String buildName = db.getBuildName(myBuildId);

        String result = projectName + "_" + buildConfigurationName + "_" + buildName + "." +
                (preferences.isBuildLogArchived() ? "zip" : "log");

        return result.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
