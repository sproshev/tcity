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

package com.tcity.android.obj;

import com.tcity.android.Status;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class BuildInfo {

    @Nullable
    public Status status;

    @Nullable
    public String result;

    @Nullable
    public String branch;

    public boolean isBranchDefault;

    @Nullable
    public String waitReason;

    @Nullable
    public Date queued;

    @Nullable
    public Date started;

    @Nullable
    public Date finished;

    @Nullable
    public String agent;
}
