package com.bose.wearable.sample.dialogs;

//
//  SamplePeriodDialog.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/18/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.bose.wearable.sample.R;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.List;

public class SamplePeriodDialog extends DialogFragment {
    public static final String ARG_SUPPORTED_PERIODS = "supported-periods";
    public static final String ARG_SENSOR_TYPE = "sensor-type";
    public static final String EXTRA_PERIOD = "sample-period-dialog-extra-period";
    public static final String EXTRA_SENSOR_TYPE = "sample-period-dialog-extra-sensor";

    private short[] mSupportedPeriods;
    @Nullable
    private SensorType mSensorType;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mSupportedPeriods = args.getShortArray(ARG_SUPPORTED_PERIODS);
            mSensorType = (SensorType) args.getSerializable(ARG_SENSOR_TYPE);
        }

        if (mSupportedPeriods == null) {
            throw new IllegalArgumentException("Supported periods not provided");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Context context = requireContext();
        final String[] labels = periodLabels(context, mSupportedPeriods);

        return new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sample_period_dialog_title_all))
            .setItems(labels, (dialogInterface, i) -> onSamplePeriodSelected(mSupportedPeriods[i]))
            .setNegativeButton(getString(R.string.cancel), null)
            .create();
    }

    public static SamplePeriodDialog newInstance(@NonNull final List<SamplePeriod>samplePeriods) {
        return newInstance(samplePeriods, null);
    }

    public static SamplePeriodDialog newInstance(@NonNull final List<SamplePeriod>samplePeriods,
                                                 @Nullable final SensorType sensorType) {
        final short[] array = new short[samplePeriods.size()];
        for (int i = 0; i < samplePeriods.size(); i++) {
            array[i] = samplePeriods.get(i).milliseconds();
        }

        final Bundle args = new Bundle();
        args.putShortArray(ARG_SUPPORTED_PERIODS, array);
        if (sensorType != null) {
            args.putSerializable(ARG_SENSOR_TYPE, sensorType);
        }

        final SamplePeriodDialog dialog = new SamplePeriodDialog();
        dialog.setArguments(args);

        return dialog;
    }

    private static String[] periodLabels(@NonNull final Context context,
                                         @NonNull final short[] milliseconds) {
        final String[] labels = new String[milliseconds.length];
        for (int i = 0; i < milliseconds.length; i++) {
            switch (milliseconds[i]) {
                case 320:
                    labels[i] = context.getString(R.string.sample_period_320);
                    break;
                case 160:
                    labels[i] = context.getString(R.string.sample_period_160);
                    break;
                case 80:
                    labels[i] = context.getString(R.string.sample_period_80);
                    break;
                case 40:
                    labels[i] = context.getString(R.string.sample_period_40);
                    break;
                case 20:
                    labels[i] = context.getString(R.string.sample_period_20);
                    break;
                case 10:
                    labels[i] = context.getString(R.string.sample_period_10);
                    break;
                case 5:
                    labels[i] = context.getString(R.string.sample_period_5);
                    break;
                default:
                    labels[i] = "<unknown>";
                    break;
            }
        }

        return labels;
    }

    private void onSamplePeriodSelected(final short millis) {
        final Fragment target = getTargetFragment();
        if (target != null) {
            final Intent intent = new Intent()
                .putExtra(EXTRA_PERIOD, SamplePeriod.fromMillis(millis));

            if (mSensorType != null) {
                intent.putExtra(EXTRA_SENSOR_TYPE, mSensorType);
            }

            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }
}
