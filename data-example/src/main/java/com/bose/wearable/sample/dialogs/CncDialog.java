package com.bose.wearable.sample.dialogs;

//
//  CncDialog.java
//  BoseWearable
//
//  Created by Tambet Ingo on 03/29/2019.
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
import com.bose.wearable.services.bmap.CncValue;

public class CncDialog extends DialogFragment {
    public static final String ARG_CURRENT_LEVEL = "current-level";
    public static final String ARG_STEPS = "steps";
    public static final String ARG_ENABLED = "enabled";
    public static final String EXTRA_LEVEL = "cnc-dialog-extra-level";
    public static final String EXTRA_ENABLED = "cnc-dialog-extra-enabled";

    private int mSteps;
    private int mCurrent;
    private boolean mEnabled;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mSteps = args.getInt(ARG_STEPS);
            mCurrent = args.getInt(ARG_CURRENT_LEVEL);
            mEnabled = args.getBoolean(ARG_ENABLED);
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        // Adjust the name of the label to match the numbers reported by the voice prompts.
        //
        // If there are 11 levels...
        //
        // Level 0 in the API indicates the highest level of noise cancellation.
        // The voice prompts on the product will say it is Level 10.
        //
        // Level 10 in the API indicates the lowest level of noise cancellation.
        // The voice prompts on the product will say it is Level 0.
        //
        // Rather than show the API levels in the UI, we show the values that match
        // the voice prompts.
        final String[] labels = new String[mSteps];
        for (int i = 0; i < mSteps; i++) {
            labels[i] = String.valueOf(mSteps - (i + 1));
        }
        labels[0] = labels[0] + " (Max)";
        labels[mSteps - 1] = labels[mSteps - 1] + " (Min)";

        return new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cnc_dialog_title))
            .setSingleChoiceItems(labels, mCurrent, (dialogInterface, i) -> onLevelSelected(i))
            .setPositiveButton(getString(R.string.cnc_enable), (dialogInterface, i) -> sendResult(true))
            .setNegativeButton(getString(R.string.cnc_disable), (dialogInterface, i) -> sendResult(false))
            .setNeutralButton(getString(R.string.cancel), null)
            .create();
    }

    public static CncDialog newInstance(@NonNull final CncValue current) {
        final Bundle args = new Bundle();
        args.putInt(ARG_STEPS, current.steps());
        args.putInt(ARG_CURRENT_LEVEL, current.currentStep());
        args.putBoolean(ARG_ENABLED, current.enabled());

        final CncDialog dialog = new CncDialog();
        dialog.setArguments(args);

        return dialog;
    }

    private void onLevelSelected(final int level) {
        mCurrent = level;
    }

    private void sendResult(final boolean enable) {
        final Fragment target = getTargetFragment();
        if (target != null) {
            final Intent intent = new Intent()
                .putExtra(EXTRA_LEVEL, mCurrent)
                .putExtra(EXTRA_ENABLED, enable);

            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }
}
