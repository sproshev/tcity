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

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tcity.android.R;

import org.jetbrains.annotations.NotNull;

public class BuildTestsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @NotNull
    private String myBuildId;

    @NotNull
    private SwipeRefreshLayout myLayout;

    // LIFECYCLE - Begin

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.overview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myBuildId = getArguments().getString(BuildHostActivity.ID_INTENT_KEY);

        myLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.overview);
        myLayout.setColorSchemeResources(R.color.green, R.color.red);
        myLayout.setOnRefreshListener(this);
    }

    // LIFECYCLE - End

    @Override
    public void onRefresh() {
    }
}
