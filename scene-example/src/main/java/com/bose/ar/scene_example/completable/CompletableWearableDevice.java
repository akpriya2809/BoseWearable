package com.bose.ar.scene_example.completable;

//
//  CompletableWearableDevice.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/29/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import com.bose.wearable.BoseWearableException;
import com.bose.wearable.services.wearablesensor.GestureConfiguration;
import com.bose.wearable.services.wearablesensor.SensorConfiguration;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;
import com.bose.wearable.wearabledevice.BaseWearableDeviceListener;
import com.bose.wearable.wearabledevice.WearableDevice;
import com.bose.wearable.wearabledevice.WearableDeviceListener;

import java.util.concurrent.CompletableFuture;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class CompletableWearableDevice {
    private final WearableDevice mWrapped;

    public CompletableWearableDevice(@NonNull final WearableDevice wearableDevice) {
        mWrapped = wearableDevice;
    }

    public void addListener(@NonNull final WearableDeviceListener listener) {
        mWrapped.addListener(listener);
    }

    public boolean removeListener(@NonNull final WearableDeviceListener listener) {
        return mWrapped.removeListener(listener);
    }

    public String name() {
        return mWrapped.name();
    }

    public boolean hasFocus() {
        return mWrapped.hasFocus();
    }

    public WearableDeviceInformation wearableDeviceInformation() {
        return mWrapped.wearableDeviceInformation();
    }

    public CompletableFuture<WearableDeviceInformation> refreshWearableDeviceInformation() {
        final CompletableFuture<WearableDeviceInformation> future = new CompletableFuture<>();
        final WearableDeviceListener listener = new BaseWearableDeviceListener() {
            @Override
            public void onWearableDeviceInfoRead(@NonNull final WearableDeviceInformation deviceInformation) {
                future.complete(deviceInformation);
            }
        };

        mWrapped.addListener(listener);
        mWrapped.refreshWearableDeviceInformation();

        return future.whenComplete((c, throwable) -> mWrapped.removeListener(listener));
    }

    public LiveData<Boolean> monitorSuspended() {
        return new SuspendedMonitor(mWrapped);
    }

    public SensorConfiguration sensorConfiguration() {
        return mWrapped.sensorConfiguration();
    }

    public GestureConfiguration gestureConfiguration() {
        return mWrapped.gestureConfiguration();
    }

    public CompletableFuture<SensorConfiguration> refreshSensors() {
        final CompletableFuture<SensorConfiguration> future = sensorConfigChangeFuture();
        mWrapped.refreshSensorConfiguration();
        return future;
    }

    public CompletableFuture<SensorConfiguration> changeSensors(@NonNull final SensorConfiguration config) {
        final CompletableFuture<SensorConfiguration> future = sensorConfigChangeFuture();
        mWrapped.changeSensorConfiguration(config);
        return future;
    }

    public CompletableFuture<GestureConfiguration> refreshGestures() {
        final CompletableFuture<GestureConfiguration> future = gestureConfigChangeFuture();
        mWrapped.refreshGestureConfiguration();
        return future;
    }

    public CompletableFuture<GestureConfiguration> changeGestures(@NonNull final GestureConfiguration config) {
        final CompletableFuture<GestureConfiguration> future = gestureConfigChangeFuture();
        mWrapped.changeGestureConfiguration(config);
        return future;
    }

    public CompletableFuture<Boolean> requestFocus() {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        final WearableDeviceListener listener = new BaseWearableDeviceListener() {
            @Override
            public void onFocusGained() {
                future.complete(true);
            }

            @Override
            public void onFocusRequestFailed(@NonNull final BoseWearableException wearableException) {
                future.completeExceptionally(wearableException);
            }
        };
        mWrapped.addListener(listener);
        mWrapped.requestFocus();

        return future.whenComplete((c, throwable) -> mWrapped.removeListener(listener));
    }

    private CompletableFuture<SensorConfiguration> sensorConfigChangeFuture() {
        final CompletableFuture<SensorConfiguration> future = new CompletableFuture<>();
        final WearableDeviceListener listener = new BaseWearableDeviceListener() {
            @Override
            public void onSensorConfigurationRead(@NonNull final SensorConfiguration sensorConfiguration) {
                future.complete(sensorConfiguration);
            }

            @Override
            public void onSensorConfigurationError(@NonNull final BoseWearableException wearableException) {
                future.completeExceptionally(wearableException);
            }
        };

        mWrapped.addListener(listener);
        return future.whenComplete((c, throwable) -> mWrapped.removeListener(listener));
    }

    private CompletableFuture<GestureConfiguration> gestureConfigChangeFuture() {
        final CompletableFuture<GestureConfiguration> future = new CompletableFuture<>();
        final WearableDeviceListener listener = new BaseWearableDeviceListener() {
            @Override
            public void onGestureConfigurationRead(@NonNull final GestureConfiguration gestureConfiguration) {
                future.complete(gestureConfiguration);
            }

            @Override
            public void onGestureConfigurationError(@NonNull final BoseWearableException wearableException) {
                future.completeExceptionally(wearableException);
            }
        };

        mWrapped.addListener(listener);
        return future.whenComplete((aVoid, throwable) -> mWrapped.removeListener(listener));
    }
}
