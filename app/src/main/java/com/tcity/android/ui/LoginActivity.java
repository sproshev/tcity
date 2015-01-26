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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;
import com.tcity.android.background.rest.RestClient;
import com.tcity.android.background.runnable.chain.ExecutableRunnableChain;
import com.tcity.android.background.runnable.chain.RunnableChain;
import com.tcity.android.background.runnable.primitive.LoginRunnable;
import com.tcity.android.ui.LoginChainListener.LoginResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoginActivity extends Activity {

    @NotNull
    private static final String TEMP_PREFERENCES_NAME = "login_activity";

    @NotNull
    private static final String URL_PREFERENCES_KEY = "url";

    @NotNull
    private static final String LOGIN_PREFERENCES_KEY = "login";

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

    @NotNull
    private CheckBox myHttpsCheckBox;

    @Nullable
    private ExecutableRunnableChain myChain;

    @NotNull
    private LoginChainListener myChainListener;

    @NotNull
    private SharedPreferences mySharedPreferences;

    private boolean myRedirecting;

    /* LIFECYCLE - BEGIN */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        myProgressBar = (ProgressBar) findViewById(android.R.id.progress);
        myProgressBar.setVisibility(View.GONE);

        mySignInButton = (Button) findViewById(R.id.sign_in_submit);
        mySignInButton.setOnClickListener(new SignInClickListener());

        myUrlEditText = (EditText) findViewById(R.id.sign_in_url);
        myLoginEditText = (EditText) findViewById(R.id.sign_in_login);
        myPasswordEditText = (EditText) findViewById(R.id.sign_in_password);
        myHttpsCheckBox = (CheckBox) findViewById(R.id.sign_in_https);

        myChainListener = calculateChainListener();

        mySharedPreferences = getSharedPreferences(TEMP_PREFERENCES_NAME, MODE_PRIVATE);
        myUrlEditText.setText(mySharedPreferences.getString(URL_PREFERENCES_KEY, null));
        myLoginEditText.setText(mySharedPreferences.getString(LOGIN_PREFERENCES_KEY, null));

        myRedirecting = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        LoginResult loginResult = myChainListener.getResult();

        if (loginResult == LoginResult.SUCCESS) {
            onSuccessfulLogin();
        } else {
            if (loginResult == LoginResult.RUNNING) {
                onRunningLogin();
            }

            if (loginResult == LoginResult.FAILED) {
                onFailedLogin();
            }

            myChainListener.setActivity(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        myChainListener.setActivity(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!myRedirecting) {
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString(URL_PREFERENCES_KEY, getText(myUrlEditText));
            editor.putString(LOGIN_PREFERENCES_KEY, getText(myLoginEditText));
            editor.apply();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object onRetainNonConfigurationInstance() {
        return myChainListener;
    }

    /* LIFECYCLE - END */

    void onRunningLogin() {
        setRefreshing(true);
    }

    void onSuccessfulLogin() {
        mySharedPreferences.edit().clear().apply();

        Intent intent = new Intent(this, SplashActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        myRedirecting = true;

        startActivity(intent);
    }

    void onFailedLogin() {
        setRefreshing(false);

        new Preferences(this).reset();

        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = myChainListener.getException();

        if (e != null) {
            myChainListener.resetException();

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @NotNull
    private LoginChainListener calculateChainListener() {
        //noinspection deprecation
        LoginChainListener result = (LoginChainListener) getLastNonConfigurationInstance();

        return result != null ? result : new LoginChainListener();
    }

    private void setRefreshing(boolean refreshing) {
        if (refreshing) {
            mySignInButton.setVisibility(View.GONE);
            myProgressBar.setVisibility(View.VISIBLE);
        } else {
            mySignInButton.setVisibility(View.VISIBLE);
            myProgressBar.setVisibility(View.GONE);
        }

        myUrlEditText.setEnabled(!refreshing);
        myLoginEditText.setEnabled(!refreshing);
        myPasswordEditText.setEnabled(!refreshing);
        myHttpsCheckBox.setEnabled(!refreshing);
    }

    private void refresh() {
        saveCredentials();

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

    private void saveCredentials() {
        String url = getText(myUrlEditText);
        String login = getText(myLoginEditText);
        String password = getText(myPasswordEditText);

        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(URL_PREFERENCES_KEY, url);
        editor.putString(LOGIN_PREFERENCES_KEY, login);
        editor.apply();

        Preferences preferences = new Preferences(LoginActivity.this);

        if (myHttpsCheckBox.isChecked()) {
            preferences.setUrl("https://" + url);
        } else {
            preferences.setUrl("http://" + url);
        }

        preferences.setAuth(login, password);
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

    @NotNull
    private String getText(@NotNull EditText editText) {
        return editText.getText().toString().trim();
    }

    private class SignInClickListener implements View.OnClickListener {

        @Override
        public void onClick(@NotNull View v) {
            if (myUrlEditText.length() == 0 || getText(myUrlEditText).length() == 0) {
                showToast(R.string.url_is_empty);
            } else if (myLoginEditText.length() == 0 || getText(myLoginEditText).length() == 0) {
                showToast(R.string.login_is_empty);
            } else if (myPasswordEditText.length() == 0 || getText(myPasswordEditText).length() == 0) {
                showToast(R.string.password_is_empty);
            } else {
                refresh();
            }
        }

        private void showToast(int resId) {
            Toast.makeText(LoginActivity.this, resId, Toast.LENGTH_LONG).show();
        }
    }
}
