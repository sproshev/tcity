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

package com.tcity.android.ui.preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.tcity.android.R;
import com.tcity.android.app.Application;
import com.tcity.android.app.Preferences;
import com.tcity.android.sync.SyncUtils;
import com.tcity.android.ui.SplashActivity;

import org.jetbrains.annotations.NotNull;

public class LogoutDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.logout)
                .setNegativeButton(R.string.no, this)
                .setPositiveButton(R.string.yes, this)
                .setMessage(R.string.logout_message);

        return builder.create();
    }

    @Override
    public void onClick(@NotNull DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                Activity activity = getActivity();

                SyncUtils.disableSync(activity);

                ((Application) activity.getApplication()).getDB().reset();
                new Preferences(activity).reset();

                Intent intent = new Intent(activity, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                break;
            default:
                dismiss();
                break;
        }
    }
}
