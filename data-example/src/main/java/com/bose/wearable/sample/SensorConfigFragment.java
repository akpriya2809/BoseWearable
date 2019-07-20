package com.bose.wearable.sample;

//
//  SensorConfListFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/18/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bose.wearable.sample.dialogs.SensorSamplePeriodDialog;
import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.sample.views.TwoItemButton;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorConfiguration;
import com.bose.wearable.services.wearablesensor.SensorInformation;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SensorConfigFragment extends Fragment {
    private static final int REQUEST_CODE_SAMPLE_PERIOD = 1;
    private SessionViewModel mViewModel;
    private ViewGroup mSensorsList;
    @NonNull
    private SensorInformation mSensorInfo = SensorInformation.EMPTY;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor_conf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSensorsList = view.findViewById(R.id.sensors_list);

        final Button button = view.findViewById(R.id.disable_all_button);
        button.setOnClickListener(v -> onDisableAllClicked());
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableSensorInfo()
            .observe(this, info -> mSensorInfo = info);

        mViewModel.wearableSensorConfiguration()
            .observe(this, this::onSensorsRead);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                refreshData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SAMPLE_PERIOD:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final SensorType sensorType = (SensorType) data.getSerializableExtra(SensorSamplePeriodDialog.EXTRA_SENSOR_TYPE);
                    final short millis = data.getShortExtra(SensorSamplePeriodDialog.EXTRA_PERIOD_MILLIS, (short) 0);
                    mViewModel.enableSensor(sensorType, millis);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void refreshData() {
        mViewModel.refreshSensorConfigurations();
    }

    private void onSensorsRead(@NonNull final SensorConfiguration sensorConfiguration) {
        mSensorsList.removeAllViews();

        final Context context = mSensorsList.getContext();

        for (final SensorType sensorType : sensorConfiguration.allSensors()) {
            final TwoItemButton button = new TwoItemButton(context);
            button.title(sensorType.toString());

            final SamplePeriod samplePeriod = sensorConfiguration.sensorSamplePeriod(sensorType);
            final short millis = samplePeriod != null ? samplePeriod.milliseconds() : 0;
            button.value(context.getString(R.string.sample_period_ms, millis));
            button.setOnClickListener(v -> onSensorClicked(sensorType));
            mSensorsList.addView(button);
        }
    }

    private void onSensorClicked(@NonNull final SensorType sensorType) {
        final List<SamplePeriod> periods = new ArrayList<>(mSensorInfo.availableSamplePeriods(sensorType));
        Collections.sort(periods, (a, b) -> a.milliseconds() > b.milliseconds() ? 1 : 0);

        final SensorSamplePeriodDialog dialog = SensorSamplePeriodDialog.newInstance(sensorType,
            periods);
        dialog.setTargetFragment(this, REQUEST_CODE_SAMPLE_PERIOD);
        dialog.show(getFragmentManager(), "sensor-sample-period-dialog");
    }

    private void onDisableAllClicked() {
        mViewModel.disableAllSensors();
    }
}
