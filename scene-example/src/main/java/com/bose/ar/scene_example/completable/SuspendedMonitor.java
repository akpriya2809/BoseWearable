package com.bose.ar.scene_example.completable;

//
//  SuspendedMonitor.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/25/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import com.bose.wearable.services.wearablesensor.SensorsSuspensionReason;
import com.bose.wearable.wearabledevice.BaseWearableDeviceListener;
import com.bose.wearable.wearabledevice.WearableDevice;
import com.bose.wearable.wearabledevice.WearableDeviceListener;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

class SuspendedMonitor extends LiveData<Boolean> {
    private final WearableDevice mDevice;
    private final WearableDeviceListener mListener;

    SuspendedMonitor(@NonNull final WearableDevice device) {
        mDevice = device;

        mListener = new BaseWearableDeviceListener() {
            @Override
            public void onSensorsSuspended(@NonNull final SensorsSuspensionReason suspensionReason) {
                setValue(true);
            }

            @Override
            public void onSensorsResumed() {
                setValue(false);
            }
        };

        setValue(mDevice.suspended());
    }

    @Override
    protected void onActive() {
        mDevice.addListener(mListener);
    }

    @Override
    protected void onInactive() {
        mDevice.removeListener(mListener);
    }
}
