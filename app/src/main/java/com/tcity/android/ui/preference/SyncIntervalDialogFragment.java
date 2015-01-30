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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.app.Preferences;
import com.tcity.android.sync.SyncUtils;

import org.jetbrains.annotations.NotNull;

public class SyncIntervalDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @NotNull
    private Preferences myPreferences;

    @NotNull
    private NumberPicker myNumberPicker;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.sync_interval_fragment, null);

        myPreferences = new Preferences(getActivity());

        myNumberPicker = (NumberPicker) view.findViewById(R.id.sync_interval_picker);
        myNumberPicker.setMinValue(SyncUtils.MIN_INTERVAL);
        myNumberPicker.setMaxValue(SyncUtils.MAX_INTERVAL);
        myNumberPicker.setValue(myPreferences.getSyncInterval());

        TextView minValueView = (TextView) view.findViewById(R.id.sync_interval_min_value);
        TextView maxValueView = (TextView) view.findViewById(R.id.sync_interval_max_value);

        minValueView.setText("Min value: " + SyncUtils.MIN_INTERVAL + " minutes");
        maxValueView.setText("Max value: " + SyncUtils.MAX_INTERVAL + " minutes");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.synchronization_interval)
                .setPositiveButton(R.string.save, this)
                .setNeutralButton(R.string.cancel, this)
                .setView(view);

        return builder.create();
    }

    @Override
    public void onClick(@NotNull DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                int value = myNumberPicker.getValue();

                if (value != myPreferences.getSyncInterval()) {
                    myPreferences.setSyncInterval(value);

                    SyncUtils.updateSyncInterval(
                            getActivity(), value, myPreferences.isSyncWifiOnly()
                    );
                }
            default:
                dismiss();
                break;
        }
    }
}
