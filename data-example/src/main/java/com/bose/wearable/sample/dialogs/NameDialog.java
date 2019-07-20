package com.bose.wearable.sample.dialogs;

//
//  NameDialog.java
//  BoseWearable
//
//  Created by Tambet Ingo on 04/01/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.bose.wearable.sample.R;

public class NameDialog extends DialogFragment {
    public static final String EXTRA_NAME = "name-dialog-extra-name";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final View viewInflated = LayoutInflater.from(getContext())
            .inflate(R.layout.name_dialog, null, false);
        final EditText input = viewInflated.findViewById(R.id.input);

        return new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.name_dialog_title))
            .setView(viewInflated)
            .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> onNameEntered(input.getText().toString()))
            .setNegativeButton(getString(R.string.cancel), null)
            .create();
    }

    public static NameDialog newInstance() {
        return new NameDialog();
    }

    private void onNameEntered(@NonNull final String name) {
        final Fragment target = getTargetFragment();
        if (target != null) {
            final Intent intent = new Intent()
                .putExtra(EXTRA_NAME, name);
            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }
}
