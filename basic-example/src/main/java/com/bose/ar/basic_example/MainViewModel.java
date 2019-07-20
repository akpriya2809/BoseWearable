package com.bose.ar.basic_example;

//
//  MainViewModel.java
//  BoseWearable
//
//  Created by Tambet Ingo on 12/10/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bose.blecore.DeviceException;
import com.bose.blecore.DiscoveredDevice;
import com.bose.blecore.Session;
import com.bose.blecore.SessionDelegate;
import com.bose.wearable.BoseWearable;
import com.bose.wearable.BoseWearableException;
import com.bose.wearable.WearableDeviceException;
import com.bose.wearable.sensordata.GestureIntent;
import com.bose.wearable.sensordata.SensorIntent;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorConfiguration;
import com.bose.wearable.services.wearablesensor.SensorType;
import com.bose.wearable.services.wearablesensor.SensorsSuspensionReason;
import com.bose.wearable.wearabledevice.BaseWearableDeviceListener;
import com.bose.wearable.wearabledevice.WearableDevice;
import com.bose.wearable.wearabledevice.WearableDeviceListener;

import java.util.Collections;
import java.util.Set;

public class MainViewModel extends ViewModel {
    private static final SamplePeriod SAMPLE_PERIOD = SamplePeriod._20_MS;

    @NonNull
    private final BoseWearable mBoseWearable;
    @NonNull
    private final MutableLiveData<SensorValue> mAccelerometerData = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<SensorValue> mRotationData = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> mBusy = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Event<DeviceException>> mErrors = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> mSensorsSuspended = new MutableLiveData<>();
    @Nullable
    private DiscoveredDevice mSelectedDevice;
    private boolean mSimulatedDevice;
    @Nullable
    private Session mSession;

    private final WearableDeviceListener mSensorDataListener = new BaseWearableDeviceListener() {
        @Override
        public void onSensorDataRead(@NonNull final SensorValue sensorValue) {
            Log.d("LLLL", "index=" + sensorValue.sensorType().toString());
            switch (sensorValue.sensorType()) {


                case ACCELEROMETER:
                    mAccelerometerData.setValue(sensorValue);
                    break;
                case ROTATION_VECTOR:
                    mRotationData.setValue(sensorValue);
                    break;
            }
        }
    };

    public MainViewModel() {
        mBoseWearable = BoseWearable.getInstance();
    }

    @Override
    protected void onCleared() {
        stopSession();
        super.onCleared();
    }

    public DiscoveredDevice selectedDevice() {
        return mSelectedDevice;
    }

    public void selectDevice(@NonNull final String deviceAddress) {
        final DiscoveredDevice device = mBoseWearable.bluetoothManager()
                .deviceByAddress(deviceAddress);

        if (sameDevice(mSelectedDevice, device)) {
            return;
        }

        stopSession();

        mSelectedDevice = device;
        mSimulatedDevice = false;

        if (mSelectedDevice == null) {
            return;
        }

        final Session session = mBoseWearable.bluetoothManager().session(mSelectedDevice);
        onSessionCreated(session);
    }

    public void selectSimulatedDevice() {
        if (mSimulatedDevice) {
            return;
        }

        if (mSession != null) {
            mSession.close();
        }

        mSelectedDevice = null;
        mSimulatedDevice = true;
        onSessionCreated(mBoseWearable.createSimulatedSession());
    }

    private void onSessionCreated(@NonNull final Session session) {
        mSession = session;
        mSession.callback(new SessionDelegate() {
            @Override
            public void sessionConnected(@NonNull final Session session) {
                final WearableDevice wearableDevice = (WearableDevice) session.device();
                if (wearableDevice != null) {
                    startMonitoring(wearableDevice);
                }
            }

            @Override
            public void sessionClosed(final int statusCode) {
                if (statusCode == 0) {
                    // Closed normally
                    mSelectedDevice = null;
                    mSimulatedDevice = false;
                    stopSession();
                } else {
                    sessionError(parseStatusCode(statusCode));
                }
            }

            @Override
            public void sessionError(@NonNull final DeviceException exception) {
                mSelectedDevice = null;
                mSimulatedDevice = false;
                mBusy.setValue(false);
                stopSession();

                mErrors.setValue(new Event<>(exception));
            }
        });

        mBusy.setValue(true);
        mSession.open();
    }

    public LiveData<Boolean> busy() {
        return mBusy;
    }

    public LiveData<Event<DeviceException>> errors() {
        return mErrors;
    }

    public LiveData<Boolean> sensorsSuspended() {
        return mSensorsSuspended;
    }

    public LiveData<SensorValue> accelerometerData() {
        return mAccelerometerData;
    }

    public LiveData<SensorValue> rotationData() {
        return mRotationData;
    }

    public static SensorIntent sensorIntent() {
        final Set<SensorType> sensorTypes = new ArraySet<>(2);
        sensorTypes.add(SensorType.ACCELEROMETER);
        sensorTypes.add(SensorType.ROTATION_VECTOR);

        return new SensorIntent(sensorTypes, Collections.singleton(SAMPLE_PERIOD));
    }

    public static GestureIntent gestureIntent() {
        return GestureIntent.EMPTY;
    }

    private void startMonitoring(@NonNull final WearableDevice wearableDevice) {
        wearableDevice.addListener(new BaseWearableDeviceListener() {
            @Override
            public void onSensorsSuspended(@NonNull final SensorsSuspensionReason suspensionReason) {
                mSensorsSuspended.setValue(true);
            }

            @Override
            public void onSensorsResumed() {
                mSensorsSuspended.setValue(false);
            }

            @Override
            public void onSensorConfigurationRead(@NonNull final SensorConfiguration sensorConfiguration) {
                done();
            }

            @Override
            public void onSensorConfigurationError(@NonNull final BoseWearableException wearableException) {
                done();
                mErrors.setValue(new Event<>(wearableException));
            }

            private void done() {
                wearableDevice.removeListener(this);
                mBusy.setValue(false);
            }
        });

        mSensorsSuspended.setValue(wearableDevice.suspended());

        final SensorConfiguration sensorConfiguration = wearableDevice.sensorConfiguration()
            .disableAll()
            .enableSensor(SensorType.ACCELEROMETER, SAMPLE_PERIOD)
            .enableSensor(SensorType.ROTATION_VECTOR, SAMPLE_PERIOD);

        wearableDevice.addListener(mSensorDataListener);
        wearableDevice.changeSensorConfiguration(sensorConfiguration);
    }

    private void stopMonitoring(@NonNull final WearableDevice wearableDevice,
                                @NonNull final Runnable onCompleted) {
        wearableDevice.removeListener(mSensorDataListener);

        wearableDevice.addListener(new BaseWearableDeviceListener() {
            @Override
            public void onSensorConfigurationRead(@NonNull final SensorConfiguration sensorConfiguration) {
                done();
            }

            @Override
            public void onSensorConfigurationError(@NonNull final BoseWearableException wearableException) {
                done();
            }

            private void done() {
                wearableDevice.removeListener(this);
                onCompleted.run();
            }
        });

        final SensorConfiguration sensorConfiguration = wearableDevice.sensorConfiguration()
            .disableAll();
        wearableDevice.changeSensorConfiguration(sensorConfiguration);
    }

    private void stopSession() {
        final Session session = mSession;
        if (session == null) {
            return;
        }

        mSession = null;
        mSensorsSuspended.setValue(false);

        final Runnable onDeviceStopped = () -> {
            session.callback(null);
            session.close();

            mBoseWearable.bluetoothManager()
                .removeSession(session);
        };

        final WearableDevice wearableDevice = (WearableDevice) session.device();
        if (wearableDevice != null) {
            stopMonitoring(wearableDevice, onDeviceStopped);
        } else {
            onDeviceStopped.run();
        }
    }

    private boolean sameDevice(@Nullable final DiscoveredDevice a,
                               @Nullable final DiscoveredDevice b) {
        if (a == null && b != null) {
            return false;
        }
        if (b == null && a != null) {
            return false;
        }
        if (a == null) {
            return true;
        }

        final String aAddress = a.bluetoothDevice().getAddress();
        final String bAddress = b.bluetoothDevice().getAddress();
        return aAddress.equals(bAddress);
    }

    @NonNull
    private static DeviceException parseStatusCode(final int statusCode) {
        switch (statusCode) {
            case 8:
                return BoseWearableException.deviceOutOfRange();
            case 19:
                return DeviceException.disconnectedByDevice();
            case 62:
            case 133:
                return BoseWearableException.deviceNotFound();
            default:
                return WearableDeviceException.fromCode(WearableDeviceException.UNKNOWN);
        }
    }
}
