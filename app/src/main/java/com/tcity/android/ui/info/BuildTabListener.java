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
import android.app.FragmentTransaction;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BuildTabListener<T extends Fragment> implements ActionBar.TabListener {

    @NotNull
    private final Activity myActivity;

    @NotNull
    private final Class<T> myClass;

    @NotNull
    private final String myTag;

    @NotNull
    private final String myBuildId;

    @Nullable
    private Fragment myFragment;

    BuildTabListener(@NotNull Activity activity,
                     @NotNull Class<T> cls,
                     @NotNull String tag,
                     @NotNull String buildId) {
        myActivity = activity;
        myClass = cls;
        myTag = tag;
        myBuildId = buildId;

        myFragment = myActivity.getFragmentManager().findFragmentByTag(myTag);
    }

    @Override
    public void onTabSelected(@Nullable ActionBar.Tab tab, @NotNull FragmentTransaction ft) {
        if (myFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BuildHostActivity.ID_INTENT_KEY, myBuildId);

            myFragment = Fragment.instantiate(myActivity, myClass.getName(), bundle);
            ft.replace(android.R.id.content, myFragment, myTag);
        } else {
            ft.attach(myFragment);
        }
    }

    @Override
    public void onTabUnselected(@Nullable ActionBar.Tab tab, @NotNull FragmentTransaction ft) {
        if (myFragment != null) {
            ft.detach(myFragment);
        }
    }

    @Override
    public void onTabReselected(@Nullable ActionBar.Tab tab, @Nullable FragmentTransaction ft) {
    }

    boolean onBackPressed() {
        return myFragment instanceof BuildArtifactsFragment &&
                ((BuildArtifactsFragment) myFragment).onBackPressed();
    }
}
