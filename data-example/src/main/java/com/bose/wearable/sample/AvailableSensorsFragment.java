package com.bose.wearable.sample;

//
//  AvailableSensorsFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/04/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.bose.wearable.services.wearablesensor.SensorType;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AvailableSensorsFragment extends BaseWearableDeviceInfoFragment {
    private CheckedTextView mAccelerometer;
    private CheckedTextView mGyroscope;
    private CheckedTextView mRotationVector;
    private CheckedTextView mGameRotation;
    private CheckedTextView mOrientation;
    private CheckedTextView mMagnetometer;
    private CheckedTextView mUncalibratedMagnetometer;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_available_sensors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAccelerometer = view.findViewById(R.id.accelerometerText);
        mGyroscope = view.findViewById(R.id.gyroscopeText);
        mRotationVector = view.findViewById(R.id.rotationVectorText);
        mGameRotation = view.findViewById(R.id.gameRotationText);
        mOrientation = view.findViewById(R.id.orientationText);
        mMagnetometer = view.findViewById(R.id.magnetometerText);
        mUncalibratedMagnetometer = view.findViewById(R.id.uncalibratedMagnetometer);
    }

    protected void onDataUpdated(@NonNull final WearableDeviceInformation deviceInformation) {
        final Set<SensorType> available = deviceInformation.availableSensors();

        mAccelerometer.setChecked(available.contains(SensorType.ACCELEROMETER));
        mGyroscope.setChecked(available.contains(SensorType.GYROSCOPE));
        mRotationVector.setChecked(available.contains(SensorType.ROTATION_VECTOR));
        mGameRotation.setChecked(available.contains(SensorType.GAME_ROTATION_VECTOR));
        mOrientation.setChecked(available.contains(SensorType.ORIENTATION));
        mMagnetometer.setChecked(available.contains(SensorType.MAGNETOMETER));
        mUncalibratedMagnetometer.setChecked(available.contains(SensorType.UNCALIBRATED_MAGNETOMETER));
    }
}
