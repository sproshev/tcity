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

package com.tcity.android.ui

import android.view.View

public trait OverviewListener {

    fun onProjectWatchClick(id: String)

    fun onBuildConfigurationWatchClick(id: String)

    fun onBuildWatchClick(id: String)

    fun onProjectNameClick(id: String)

    fun onBuildConfigurationNameClick(id: String)

    fun onBuildNameClick(id: String)

    fun onProjectOptionsClick(id: String, anchor: View)

    fun onBuildConfigurationOptionsClick(id: String, anchor: View)

    fun onBuildOptionsClick(id: String, anchor: View)
}
