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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.background.runnable.chain.RunnableChain;
import com.tcity.android.background.runnable.primitive.LoginRunnable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoginActivity extends Activity {

    @NotNull
    private ProgressBar myProgressBar;

    @NotNull
    private Button mySignInButton;

    @NotNull
    private EditText myUrlEditText;

    @NotNull
    private EditText myLoginEditText;

    @NotNull
    private EditText myPasswordEditText;

    @Nullable
    private ExecutableRunnableChain myChain;

    @NotNull
    private ChainListener myChainListener;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        myProgressBar = (ProgressBar) findViewById(android.R.id.progress);
        myProgressBar.setVisibility(View.GONE);

        mySignInButton = (Button) findViewById(R.id.sign_in);
        mySignInButton.setOnClickListener(new SignInClickListener());

        myUrlEditText = (EditText) findViewById(R.id.url);
        myLoginEditText = (EditText) findViewById(R.id.login);
        myPasswordEditText = (EditText) findViewById(R.id.password);

        myChainListener = calculateChainListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (myChainListener.myLoginResult == LoginResult.SUCCESS) {
            Intent intent = new Intent(this, SplashActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } else {
            if (myChainListener.myLoginResult == LoginResult.RUNNING) {
                setRefreshing(true);
            }

            if (myChainListener.myLoginResult == LoginResult.FAILED) {
                new Preferences(this).reset();
            }

            myChainListener.myActivity = this;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        myChainListener.myActivity = null;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return myChainListener;
    }

    /* LIFECYCLE - END */

    @NotNull
    private ChainListener calculateChainListener() {
        ChainListener result = (ChainListener) getLastNonConfigurationInstance();

        return result != null ? result : new ChainListener();
    }

    private void setRefreshing(boolean refreshing) {
        if (refreshing) {
            mySignInButton.setVisibility(View.GONE);
            myUrlEditText.setEnabled(false);
            myLoginEditText.setEnabled(false);
            myPasswordEditText.setEnabled(false);
            myProgressBar.setVisibility(View.VISIBLE);
        } else {
            mySignInButton.setVisibility(View.VISIBLE);
            myUrlEditText.setEnabled(true);
            myLoginEditText.setEnabled(true);
            myPasswordEditText.setEnabled(true);
            myProgressBar.setVisibility(View.GONE);
        }
    }

    private void refresh() {
        String url = myUrlEditText.getText().toString();
        String login = myLoginEditText.getText().toString();
        String password = myPasswordEditText.getText().toString();

        Preferences preferences = new Preferences(LoginActivity.this);
        preferences.setUrl(url);
        preferences.setAuth(login, password);

        if (myChain == null) {
            myChain = calculateExecutableChain();
        }

        if (myChain.getStatus() != AsyncTask.Status.RUNNING) {
            if (myChain.getStatus() == AsyncTask.Status.FINISHED) {
                myChain = calculateExecutableChain();
            }

            myChainListener.onStarted();
            myChain.execute();
        }
    }

    @NotNull
    private ExecutableRunnableChain calculateExecutableChain() {
        return RunnableChain.getSingleRunnableChain(
                new LoginRunnable(
                        new RestClient(
                                new Preferences(this)
                        )
                )
        ).toAsyncTask(myChainListener);
    }

    private class SignInClickListener implements View.OnClickListener {

        @Override
        public void onClick(@NotNull View v) {
            if (myUrlEditText.length() == 0) {
                showToast(R.string.url_is_empty);
            } else if (myLoginEditText.length() == 0) {
                showToast(R.string.login_is_empty);
            } else if (myPasswordEditText.length() == 0) {
                showToast(R.string.password_is_empty);
            } else {
                refresh();
            }
        }

        private void showToast(int resId) {
            Toast.makeText(LoginActivity.this, resId, Toast.LENGTH_LONG).show();
        }
    }

    private static class ChainListener implements RunnableChain.Listener {

        @Nullable
        private LoginActivity myActivity;

        private LoginResult myLoginResult = LoginResult.PENDING;

        public void onStarted() {
            myLoginResult = LoginResult.RUNNING;

            if (myActivity != null) {
                myActivity.setRefreshing(true);
            }
        }

        @Override
        public void onFinished() {
            if (myLoginResult == LoginResult.FAILED) {
                return;
            }

            myLoginResult = LoginResult.SUCCESS;

            if (myActivity != null) {
                Intent intent = new Intent(myActivity, SplashActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                myActivity.startActivity(intent);
            }
        }

        @Override
        public void onException(@NotNull Exception e) {
            myLoginResult = LoginResult.FAILED;

            if (myActivity != null) {
                myActivity.setRefreshing(false);

                Toast.makeText(myActivity, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private static enum LoginResult {
        PENDING, RUNNING, SUCCESS, FAILED
    }
}
