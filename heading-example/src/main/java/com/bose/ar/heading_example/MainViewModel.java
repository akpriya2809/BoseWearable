package com.bose.ar.heading_example;

//
//  MainViewModel.java
//  BoseWearable
//
//  Created by Tambet Ingo on 02/19/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.annotation.SuppressLint;
import android.app.Application;
import android.hardware.GeomagneticField;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bose.blecore.DeviceException;
import com.bose.blecore.DiscoveredDevice;
import com.bose.blecore.Session;
import com.bose.blecore.SessionDelegate;
import com.bose.wearable.BoseWearable;
import com.bose.wearable.BoseWearableException;
import com.bose.wearable.WearableDeviceException;
import com.bose.wearable.sensordata.GestureIntent;
import com.bose.wearable.sensordata.Quaternion;
import com.bose.wearable.sensordata.SensorIntent;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorConfiguration;
import com.bose.wearable.services.wearablesensor.SensorType;
import com.bose.wearable.services.wearablesensor.SensorsSuspensionReason;
import com.bose.wearable.wearabledevice.BaseWearableDeviceListener;
import com.bose.wearable.wearabledevice.WearableDevice;
import com.bose.wearable.wearabledevice.WearableDeviceListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;

public class MainViewModel extends AndroidViewModel {
    private static final SamplePeriod SAMPLE_PERIOD = SamplePeriod._20_MS;
    private static final Quaternion TRANSLATION_Q = new Quaternion(1, 0, 0, 0);

    @NonNull
    private final BoseWearable mBoseWearable;
    @NonNull
    private final MutableLiveData<Double> mHeading = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Double> mAccuracy = new MutableLiveData<>();
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
    private boolean mUseTrueNorth = true;
    private final FusedLocationProviderClient mLocationClient;
    @Nullable
    private Location mLocation;

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(final LocationResult locationResult) {
            if (locationResult != null) {
                mLocation = locationResult.getLastLocation();
            }
        }
    };

    private final WearableDeviceListener mSensorDataListener = new BaseWearableDeviceListener() {
        @Override
        public void onSensorDataRead(@NonNull final SensorValue sensorValue) {
            switch (sensorValue.sensorType()) {
                case ROTATION_VECTOR:
                    final Quaternion quaternion = Quaternion.multiply(sensorValue.quaternion(), TRANSLATION_Q);
                    double heading = Math.toDegrees(-quaternion.zRotation());
                    if (mUseTrueNorth) {
                        heading = trueHeading(heading);
                    }

                    mHeading.setValue(heading);
                    mAccuracy.setValue(Math.toDegrees(sensorValue.quaternionAccuracy().estimatedAccuracy()));
                    break;
            }
        }
    };

    @SuppressLint("MissingPermission") // BoseWearable-Android ensures coarse location permission
    public MainViewModel(@NonNull final Application application) {
        super(application);
        mBoseWearable = BoseWearable.getInstance();

        mLocationClient = LocationServices.getFusedLocationProviderClient(application);

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10_000);
        locationRequest.setFastestInterval(5_000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        mLocationClient.getLastLocation().addOnSuccessListener(location -> mLocation = location);
    }

    @Override
    protected void onCleared() {
        mLocationClient.removeLocationUpdates(mLocationCallback);
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

    public LiveData<Double> heading() {
        return mHeading;
    }

    public LiveData<Double> accuracy() {
        return mAccuracy;
    }

    public boolean useTrueNorth() {
        return mUseTrueNorth;
    }

    public void useTrueNorth(final boolean enabled) {
        mUseTrueNorth = enabled;
    }

    public static SensorIntent sensorIntent() {
        return new SensorIntent(Collections.singleton(SensorType.ROTATION_VECTOR),
            Collections.singleton(SAMPLE_PERIOD));
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

    private double trueHeading(final double degrees) {
        final Location location = mLocation;
        if (location == null) {
            return degrees;
        }

        final GeomagneticField geoField = new GeomagneticField((float) location.getLatitude(),
            (float) location.getLongitude(), (float) location.getAltitude(), System.currentTimeMillis());

        double heading = degrees + geoField.getDeclination();

        // Normalize the value
        if (heading <= -180) {
            heading += 360;
        }
        if (heading > 180) {
            heading -= 360;
        }

        return heading;
    }
}
