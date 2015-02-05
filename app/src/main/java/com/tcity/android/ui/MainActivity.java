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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tcity.android.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainActivity extends Activity {

    @NotNull
    private String[] myTitles;

    @NotNull
    private DrawerListener myListener;

    @NotNull
    private DrawerLayout myDrawerLayout;

    @NotNull
    private ListView myDrawerList;

    // LIFECYCLE - Begin

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        myTitles = getResources().getStringArray(R.array.main_drawerlist);

        myDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
        myDrawerList = (ListView) findViewById(R.id.main_drawerlist);

        myListener = new DrawerListener();

        myDrawerLayout.setDrawerListener(myListener);
        myDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        myDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        myDrawerList.setAdapter(
                new ArrayAdapter<>(
                        this,
                        R.layout.drawer_item,
                        myTitles
                )
        );

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        myListener.syncState();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        myListener.onConfigurationChanged(newConfig);
    }

    // LIFECYCLE - End

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        return myListener.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        String tag = Integer.toString(position);
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString(MyFragment.INTENT_KEY, tag);

            fragment = Fragment.instantiate(this, MyFragment.class.getName(), bundle);
            ft.replace(R.id.main_content, fragment, tag);
        } else {
            ft.attach(fragment);
        }

        ft.commit();

        myDrawerList.setItemChecked(position, true);
        setTitle(myTitles[position]);
        myDrawerLayout.closeDrawer(myDrawerList);
    }

    public static class MyFragment extends Fragment {

        @NotNull
        public static final String INTENT_KEY = "intent_key";

        @Nullable
        @Override
        public View onCreateView(@NotNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            TextView view = new TextView(getActivity());

            view.setText(getArguments().getString(INTENT_KEY));

            return view;
        }
    }

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(@NotNull AdapterView<?> parent,
                                @NotNull View view,
                                int position,
                                long id) {
            selectItem(position);
        }
    }

    private class DrawerListener extends ActionBarDrawerToggle {

        public DrawerListener() {
            super(
                    MainActivity.this,
                    myDrawerLayout,
                    R.drawable.ic_drawer,
                    R.string.open_drawer,
                    R.string.close_drawer
            );
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);

            // TODO title, menu
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            // TODO title, menu
        }
    }
}
