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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    private Preferences myPreferences;

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

        myPreferences = new Preferences(this);

        myUrlEditText.setText(myPreferences.getUrl());
        myLoginEditText.setText(myPreferences.getLogin());
    }

    private void updateLoadingMode(boolean enable) {
        if (enable) {
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

    private class SignInClickListener implements View.OnClickListener {

        @Override
        public void onClick(@NotNull View v) {
            String url = myUrlEditText.getText().toString();
            String login = myLoginEditText.getText().toString();
            String password = myPasswordEditText.getText().toString();

            if (url.length() == 0) {
                showToast(R.string.url_is_empty);
            } else if (login.length() == 0) {
                showToast(R.string.login_is_empty);
            } else if (password.length() == 0) {
                showToast(R.string.password_is_empty);
            } else {
//                TODO
//                myPreferences.setUrl(url);
//                myPreferences.setAuth(login, password);
//                updateLoadingMode(true);
            }
        }

        private void showToast(int resId) {
            Toast.makeText(LoginActivity.this, resId, Toast.LENGTH_LONG).show();
        }
    }
}
