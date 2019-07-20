package com.bose.wearable.sample;

//
//  SensorInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bose.wearable.impl.Range;
import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.sample.views.TwoLineTextView;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorInformation;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.Locale;
import java.util.Set;

@SuppressWarnings("PMD.EmptyCatchBlock")
public class SensorInfoFragment extends Fragment {
    public static final String ARG_SENSOR_TYPE = "sensor-type";

    private SessionViewModel mViewModel;
    private SensorType mSensorType;
    private TwoLineTextView mScaledValueText;
    private TwoLineTextView mRawValueText;
    private TwoLineTextView mSampleLenText;
    private CheckedTextView mPeriod320;
    private CheckedTextView mPeriod160;
    private CheckedTextView mPeriod80;
    private CheckedTextView mPeriod40;
    private CheckedTextView mPeriod20;
    private CheckedTextView mPeriod10;
    private CheckedTextView mPeriod5;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mSensorType = (SensorType) args.getSerializable(ARG_SENSOR_TYPE);
        }

        if (mSensorType == null) {
            throw new IllegalArgumentException();
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TwoLineTextView typeText = view.findViewById(R.id.sensor_type_text);
        typeText.value(mSensorType.toString());

        mScaledValueText = view.findViewById(R.id.scaled_value_range_text);
        mRawValueText = view.findViewById(R.id.raw_value_range_text);
        mSampleLenText = view.findViewById(R.id.sample_length_text);
        mPeriod320 = view.findViewById(R.id.sample_320);
        mPeriod160 = view.findViewById(R.id.sample_160);
        mPeriod80 = view.findViewById(R.id.sample_80);
        mPeriod40 = view.findViewById(R.id.sample_40);
        mPeriod20 = view.findViewById(R.id.sample_20);
        mPeriod10 = view.findViewById(R.id.sample_10);
        mPeriod5 = view.findViewById(R.id.sample_5);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableSensorInfo()
            .observe(this, this::onSensorInfoRead);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                refreshData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshData() {
        mViewModel.refreshWearableSensorInformation();
    }

    private void onSensorInfoRead(@NonNull final SensorInformation sensorInformation) {
        final Range<Short> scaledRange = sensorInformation.scaledValueRange(mSensorType);
        if (scaledRange != null) {
            mScaledValueText.value(String.format(Locale.US, "%d .. %d",
                scaledRange.lower(), scaledRange.upper()));
        }

        final Range<Short> rawRange = sensorInformation.rawValueRange(mSensorType);
        if (rawRange != null) {
            mRawValueText.value(String.format(Locale.US, "%d .. %d",
                rawRange.lower(), rawRange.upper()));
        }

        mSampleLenText.value(String.valueOf(sensorInformation.sampleLength(mSensorType)));

        final Set<SamplePeriod> sp = sensorInformation.availableSamplePeriods(mSensorType);
        mPeriod320.setChecked(sp.contains(SamplePeriod._320_MS));
        mPeriod160.setChecked(sp.contains(SamplePeriod._160_MS));
        mPeriod80.setChecked(sp.contains(SamplePeriod._80_MS));
        mPeriod40.setChecked(sp.contains(SamplePeriod._40_MS));
        mPeriod20.setChecked(sp.contains(SamplePeriod._20_MS));
        mPeriod10.setChecked(sp.contains(SamplePeriod._10_MS));
        mPeriod5.setChecked(sp.contains(SamplePeriod._5_MS));
    }
}
