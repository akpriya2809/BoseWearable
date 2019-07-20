package com.bose.wearable.sample.dialogs;

//
//  AnrDialog.java
//  BoseWearable
//
//  Created by Tambet Ingo on 03/31/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.bose.wearable.sample.R;
import com.bose.wearable.services.bmap.AnrInformation;
import com.bose.wearable.services.bmap.AnrMode;

import java.util.ArrayList;
import java.util.List;

public class AnrDialog extends DialogFragment {
    public static final String ARG_CURRENT_VALUE = "current-value";
    public static final String ARG_SUPPORTED = "supported";
    public static final String EXTRA_MODE = "anr-dialog-extra-mode";

    private AnrMode mCurrent;
    private List<AnrMode> mSupported;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mCurrent = (AnrMode) args.getSerializable(ARG_CURRENT_VALUE);

            final byte[] supported = args.getByteArray(ARG_SUPPORTED);
            mSupported = new ArrayList<>(supported.length);
            for (final byte b : supported) {
                mSupported.add(AnrMode.parse(b));
            }
        }

        if (mCurrent == null) {
            throw new IllegalArgumentException();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final String[] labels = new String[mSupported.size()];
        for (int i = 0; i < mSupported.size(); i++) {
            labels[i] = mSupported.get(i).toString();
        }

        return new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.anr_dialog_title))
            .setItems(labels, (dialogInterface, i) -> onAnrModeSelected(mSupported.get(i)))
            .setNegativeButton(getString(R.string.cancel), null)
            .create();
    }

    public static AnrDialog newInstance(@NonNull final AnrInformation info) {
        final List<AnrMode> supported = info.supported();
        final byte[] array = new byte[supported.size()];
        for (int i = 0; i < supported.size(); i++) {
            array[i] = supported.get(i).value();
        }

        final Bundle args = new Bundle();
        args.putSerializable(ARG_CURRENT_VALUE, info.current());
        args.putByteArray(ARG_SUPPORTED, array);

        final AnrDialog dialog = new AnrDialog();
        dialog.setArguments(args);

        return dialog;
    }

    private void onAnrModeSelected(@NonNull final AnrMode mode) {
        final Fragment target = getTargetFragment();
        if (target != null) {
            final Intent intent = new Intent()
                .putExtra(EXTRA_MODE, mode);
            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }
}
