package com.bose.wearable.sample.dialogs;

//
//  SensorSamplePeriodDialog.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/19/2018.
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

public class SensorSamplePeriodDialog  extends DialogFragment {
    public static final String ARG_SENSOR_TYPE = "sensor-type";
    public static final String ARG_SUPPORTED_PERIODS = "supported-periods";
    public static final String EXTRA_SENSOR_TYPE = "sensor-sample-period-dialog-extra-type";
    public static final String EXTRA_PERIOD_MILLIS = "sensor-sample-period-dialog-extra-period";

    private SensorType mSensorType;
    private short[] mSupportedPeriods;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mSensorType = (SensorType) args.getSerializable(ARG_SENSOR_TYPE);
            mSupportedPeriods = args.getShortArray(ARG_SUPPORTED_PERIODS);
        }

        if (mSensorType == null || mSupportedPeriods == null) {
            throw new IllegalArgumentException("Supported periods not provided");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Context context = requireContext();
        final String[] labels = periodLabels(context, mSensorType, mSupportedPeriods);

        return new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sample_period_dialog_title_one, mSensorType.toString()))
            .setItems(labels, (dialogInterface, i) -> onSamplePeriodSelected(i))
            .setNegativeButton(getString(R.string.cancel), null)
            .create();
    }

    public static SensorSamplePeriodDialog newInstance(@NonNull final SensorType sensorType,
                                                       @NonNull final List<SamplePeriod> samplePeriods) {
        final short[] array = new short[samplePeriods.size()];
        for (int i = 0; i < samplePeriods.size(); i++) {
            array[i] = samplePeriods.get(i).milliseconds();
        }

        final Bundle args = new Bundle();
        args.putSerializable(ARG_SENSOR_TYPE, sensorType);
        args.putShortArray(ARG_SUPPORTED_PERIODS, array);

        final SensorSamplePeriodDialog dialog = new SensorSamplePeriodDialog();
        dialog.setArguments(args);

        return dialog;
    }

    private static String[] periodLabels(@NonNull final Context context,
                                         @NonNull final SensorType sensorType,
                                         @NonNull final short[] milliseconds) {
        final String[] labels = new String[milliseconds.length + 1];
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

        labels[milliseconds.length] = context.getString(R.string.sensor_config_disable_sensor,
            sensorType.toString());

        return labels;
    }

    private void onSamplePeriodSelected(final int index) {
        final short millis;
        if (index < mSupportedPeriods.length) {
            millis = mSupportedPeriods[index];
        } else {
            millis = 0;
        }

        final Fragment target = getTargetFragment();
        if (target != null) {
            final Intent intent = new Intent()
                .putExtra(EXTRA_SENSOR_TYPE, mSensorType)
                .putExtra(EXTRA_PERIOD_MILLIS, millis);

            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }
}
