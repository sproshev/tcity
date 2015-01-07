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

import com.tcity.android.background.runnable.chain.RunnableChain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class LoginChainListener implements RunnableChain.Listener {

    @Nullable
    private LoginActivity myActivity;

    @NotNull
    private LoginResult myLoginResult = LoginResult.PENDING;

    @Nullable
    private Exception myException;

    public void onStarted() {
        myLoginResult = LoginResult.RUNNING;
        myException = null;

        if (myActivity != null) {
            myActivity.onRunningLogin();
        }
    }

    @Override
    public void onFinished() {
        if (myLoginResult != LoginResult.FAILED) {
            myLoginResult = LoginResult.SUCCESS;

            if (myActivity != null) {
                myActivity.onSuccessfulLogin();
            }
        }
    }

    @Override
    public void onException(@NotNull Exception e) {
        myLoginResult = LoginResult.FAILED;
        myException = e;

        if (myActivity != null) {
            myActivity.onFailedLogin(e);
        }
    }

    @NotNull
    LoginResult getResult() {
        return myLoginResult;
    }

    @Nullable
    Exception getException() {
        return myException;
    }

    void setActivity(@Nullable LoginActivity activity) {
        myActivity = activity;
    }

    static enum LoginResult {
        PENDING, RUNNING, SUCCESS, FAILED
    }
}
