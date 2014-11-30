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

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.commonsware.cwac.merge.MergeAdapter;
import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.db.DbPackage;
import com.tcity.android.db.ProjectSchema;
import com.tcity.android.parser.ProjectsParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainActivity extends ListActivity {

    @NotNull
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @NotNull
    private Application myApplication;

    @NotNull
    private Cursor myProjectsCursor;

    @NotNull
    private Cursor myWatchedProjectsCursor;

    @NotNull
    private ConceptListener myProjectsListener;

    @NotNull
    private TaskListener myProjectsTaskListener;

    @NotNull
    private ConceptsCursorAdapter myProjectsAdapter;

    @NotNull
    private ConceptsCursorAdapter myWatchedProjectsAdapter;

    @NotNull
    private MergeAdapter myMainAdapter;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myApplication = (Application) getApplication();

        SQLiteDatabase db = myApplication.getDBHelper().getReadableDatabase();

        myProjectsCursor = db.query(ProjectSchema.INSTANCE$.getTableName(), null, null, null, null, null, null);

        myWatchedProjectsCursor = db.query(ProjectSchema.INSTANCE$.getTableName(), null, "watched = ?", new String[]{"1"}, null, null, null);

        myMainAdapter = new MergeAdapter();

//        myMainAdapter.addView();

        myProjectsListener = new ConceptListener() {
            @Override
            public void onWatchClick(@NotNull String id) {
                Log.d(LOG_TAG, id);

                myApplication.getPreferences().addWatchedProjectId(id);
                myApplication.getDBHelper().getWritableDatabase().update(ProjectSchema.INSTANCE$.getTableName(), DbPackage.contentValues(true, DbPackage.getWATCHED_COLUMN()), DbPackage.getTC_ID_COLUMN() + " = ?", new String[]{id});

                myProjectsCursor.requery();
                myWatchedProjectsCursor.requery();

                myMainAdapter.notifyDataSetChanged();
            }

            @Override
            public void onOptionsClick(@NotNull String id, @NotNull View v) {
                Log.d(LOG_TAG, id);

                // pref and db
            }
        };

        myProjectsTaskListener = new TaskListener() {
            @Override
            public void onComplete(@Nullable Exception e) {
                Log.d(LOG_TAG, "Complete");

                myProjectsCursor.requery();
                myProjectsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onUpdate() {
                Log.d(LOG_TAG, "Update");

                myProjectsCursor.requery();
                myProjectsAdapter.notifyDataSetChanged();
            }
        };

        myProjectsAdapter = new ConceptsCursorAdapter(this, myProjectsCursor, myProjectsListener);
        myWatchedProjectsAdapter = new ConceptsCursorAdapter(this, myWatchedProjectsCursor, myProjectsListener);

        myMainAdapter.addAdapter(myWatchedProjectsAdapter);
        myMainAdapter.addView(getLayoutInflater().inflate(R.layout.separator_item, getListView(), false));
        myMainAdapter.addAdapter(myProjectsAdapter);

        getListView().setAdapter(myMainAdapter);

        new ProjectsTask(
                myApplication.getDBHelper(),
                ProjectSchema.INSTANCE$,
                myProjectsTaskListener,
                ProjectsParser.INSTANCE$,
                myApplication.getPreferences()
        ).execute();
    }

    /* LIFECYCLE - END */
}
