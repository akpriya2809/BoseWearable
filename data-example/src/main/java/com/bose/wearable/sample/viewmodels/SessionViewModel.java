package com.bose.wearable.sample.viewmodels;

//
//  SessionViewModel.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/01/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.bose.blecore.DeviceException;
import com.bose.blecore.Session;
import com.bose.blecore.SessionDelegate;
import com.bose.blecore.deviceinformation.DeviceInformation;
import com.bose.wearable.BoseWearable;
import com.bose.wearable.BoseWearableException;
import com.bose.wearable.WearableDeviceException;
import com.bose.wearable.focus.FocusMode;
import com.bose.wearable.sample.Event;
import com.bose.wearable.sensordata.GestureData;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.services.bmap.AnrMode;
import com.bose.wearable.services.wearablesensor.GestureConfiguration;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorConfiguration;
import com.bose.wearable.services.wearablesensor.SensorInformation;
import com.bose.wearable.services.wearablesensor.SensorType;
import com.bose.wearable.services.wearablesensor.SensorsSuspensionReason;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;
import com.bose.wearable.wearabledevice.DeviceProperties;
import com.bose.wearable.wearabledevice.WearableDevice;
import com.bose.wearable.wearabledevice.WearableDeviceListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SessionViewModel extends ViewModel implements WearableDeviceListener {
    private static final String TAG = SessionViewModel.class.getSimpleName();
    private static final int REFRESH_TIMEOUT = 3000; // In milliseconds

    private final MutableLiveData<ConnectionState> mConnectionState = new MutableLiveData<>();
    private final MutableLiveData<DeviceInformation> mDeviceInfo = new MutableLiveData<>();
    private final MutableLiveData<DeviceProperties> mDeviceProperties = new MutableLiveData<>();
    private final MutableLiveData<WearableDeviceInformation> mWearableDeviceInfo = new MutableLiveData<>();
    private final MutableLiveData<SensorInformation> mWearableSensorInfo = new MutableLiveData<>();
    private final MutableLiveData<SensorConfiguration> mWearableSensorConfiguration = new MutableLiveData<>();
    private final MutableLiveData<GestureConfiguration> mWearableGestureConfiguration = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mSensorsSuspended = new MutableLiveData<>();

    // Sensor data
    private final MutableLiveData<SensorValue> mAccelerometerData = new MutableLiveData<>();
    private final MutableLiveData<SensorValue> mGyroscopeData = new MutableLiveData<>();
    private final MutableLiveData<SensorValue> mRotationVectorData = new MutableLiveData<>();
    private final MutableLiveData<SensorValue> mGameRotationData = new MutableLiveData<>();
    private final MutableLiveData<SensorValue> mOrientationData = new MutableLiveData<>();
    private final MutableLiveData<SensorValue> mMagnetometerData = new MutableLiveData<>();
    private final MutableLiveData<SensorValue> mUncalibratedMagnetometerData = new MutableLiveData<>();
    private final MutableLiveData<Event<GestureEvent>> _mGestureEvents = new MutableLiveData<>();
    private final List<GestureEvent> mGestureEvents = new ArrayList<>();

    private final MutableLiveData<Boolean> mFocus = new MutableLiveData<>();
    @NonNull
    private final LiveData<Event<?>> mFocusReceived;

    private final MutableLiveData<Event<DeviceException>> mErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mBusy = new MutableLiveData<>();

    private final Handler mHandler = new Handler();
    private final Queue<PendingOp> mPendingOps = new LinkedList<>();

    public SessionViewModel() {
        mConnectionState.setValue(ConnectionState.IDLE);
        mBusy.setValue(false);

        if (BoseWearable.getInstance().focusMode() == FocusMode.MANUAL) {
            final LiveData<Boolean> uniqueFocus = LiveDataUtil.distinctUntilChanged(mFocus);
            final LiveData<Boolean> receivedFocus = LiveDataUtil.filter(uniqueFocus, hasFocus -> hasFocus);
            mFocusReceived = Transformations.map(receivedFocus, gotFocus -> new Event<>(true));
        } else {
            mFocusReceived = new NeverLiveData<>();
        }
    }

    @Override
    protected void onCleared() {
        stopSession();
        super.onCleared();
    }

    public void connect(@NonNull final Destination destination) {
        if (destination.equals(currentDestination())) {
            return;
        }

        stopSession();

        final Session session = destination.createSession();
        session.callback(new SessionDelegate() {
            @Override
            public void sessionConnected(@NonNull final Session session) {
                final ConnectionState.Connected state = new ConnectionState.Connected(destination, session);
                final WearableDevice wd = state.device();
                wd.addListener(SessionViewModel.this);

                mDeviceInfo.setValue(wd.deviceInformation());
                mDeviceProperties.setValue(wd.deviceProperties());
                mWearableDeviceInfo.setValue(wd.wearableDeviceInformation());
                mWearableSensorInfo.setValue(wd.sensorInformation());
                mWearableSensorConfiguration.setValue(wd.sensorConfiguration());
                mWearableGestureConfiguration.setValue(wd.gestureConfiguration());
                mSensorsSuspended.setValue(wd.suspended());

                pendingOpMaybeDone(PendingOp.CONNECTING);
                mFocus.setValue(wd.hasFocus());
                mConnectionState.setValue(state);
            }

            @Override
            public void sessionClosed(final int statusCode) {
                final DeviceException error = parseStatusCode(statusCode);
                if (error == null) {
                    pendingOpMaybeDone(PendingOp.CONNECTING);
                    stopSession();
                } else {
                    sessionError(error);
                }
            }

            @Override
            public void sessionError(@NonNull final DeviceException exception) {
                pendingOpMaybeDone(PendingOp.CONNECTING);
                stopSession();
                mErrors.setValue(new Event<>(exception));
            }
        });

        addPendingOp(PendingOp.CONNECTING);
        mConnectionState.setValue(new ConnectionState.Connecting(destination, session));
        mBusy.setValue(true);

        session.open();
    }

    public LiveData<ConnectionState> monitorConnection() {
        return mConnectionState;
    }

    public void disconnect() {
        stopSession();
    }

    public void refreshServices() {
        final ConnectionState state = mConnectionState.getValue();
        if (state instanceof ConnectionState.Connected) {
            addPendingOp(PendingOp.REFRESH_SERVICES);
            final Session session = ((ConnectionState.Connected) state).session();
            session.refreshServices();

            // Give it a little bit of time and close the connection
            mHandler.postDelayed(() -> {
                pendingOpMaybeDone(PendingOp.REFRESH_SERVICES);
                stopSession();
            }, REFRESH_TIMEOUT);
        }
    }

    public void requestFocus() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            wearableDevice.requestFocus();
        }
    }

    public void refreshDeviceInformation() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            wearableDevice.refreshDeviceInformation();
        }
    }

    @NonNull
    public LiveData<DeviceInformation> monitorDeviceInfo() {
        return mDeviceInfo;
    }

    public void refreshDeviceProperties() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            wearableDevice.refreshDeviceProperties();
        }
    }

    @NonNull
    public LiveData<DeviceProperties> monitorDeviceProperties() {
        return mDeviceProperties;
    }

    public void changeProductName(@NonNull final String newName) {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            wearableDevice.changeName(newName);
        }
    }

    public void changeCnc(final int cncSetting, final boolean enabled) {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            wearableDevice.changeCnc(cncSetting, enabled);
        }
    }

    public void changeAnr(@NonNull final AnrMode mode) {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            wearableDevice.changeAnr(mode);
        }
    }

    public void refreshWearableDeviceInformation() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            addPendingOp(PendingOp.WEARABLE_DEVICE_INFO);
            wearableDevice.refreshWearableDeviceInformation();
        }
    }

    @NonNull
    public LiveData<WearableDeviceInformation> wearableDeviceInfo() {
        return mWearableDeviceInfo;
    }

    public void refreshWearableSensorInformation() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            addPendingOp(PendingOp.SENSOR_INFO);
            wearableDevice.refreshSensorInformation();
        }
    }

    @NonNull
    public LiveData<SensorInformation> wearableSensorInfo() {
        return mWearableSensorInfo;
    }

    @NonNull
    public LiveData<SensorConfiguration> wearableSensorConfiguration() {
        return mWearableSensorConfiguration;
    }

    public void refreshSensorConfigurations() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            addPendingOp(PendingOp.SENSOR_CONF);
            wearableDevice.refreshSensorConfiguration();
        }
    }

    // Sensors

    public List<SamplePeriod> availableSamplePeriods() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            return wearableDevice.sensorInformation()
                .availableSamplePeriods();
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    public SamplePeriod sensorSamplePeriod() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            return wearableDevice.sensorConfiguration()
                .enabledSensorsSamplePeriod();
        } else {
            return SamplePeriod._320_MS;
        }
    }

    public void sensorSamplePeriod(@NonNull final SamplePeriod samplePeriod) {
        final WearableDevice wearableDevice = device();
        if (wearableDevice == null) {
            return;
        }

        final SensorConfiguration updated = wearableDevice.sensorConfiguration()
            .enabledSensorsSamplePeriod(samplePeriod);

        addPendingOp(PendingOp.SENSOR_CONF);
        wearableDevice.changeSensorConfiguration(updated);
    }

    public void disableAllSensors() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice == null) {
            return;
        }

        final SensorConfiguration current = wearableDevice.sensorConfiguration();
        final SensorConfiguration updated = current.disableAll();

        if (!current.equals(updated)) {
            addPendingOp(PendingOp.SENSOR_CONF);
            wearableDevice.changeSensorConfiguration(updated);
        }
    }

    public void enableSensor(@NonNull final SensorType sensorType, final short millis) {
        final WearableDevice wearableDevice = device();
        if (wearableDevice == null) {
            return;
        }

        final SensorConfiguration current = wearableDevice.sensorConfiguration();
        final SamplePeriod currentPeriod = current.sensorSamplePeriod(sensorType);
        final short currentMillis = currentPeriod != null ? currentPeriod.milliseconds() : 0;
        if (currentMillis != millis) {
            final SensorConfiguration updated;
            if (millis > 0) {
                updated = current.enableSensor(sensorType, SamplePeriod.fromMillis(millis));
            } else {
                updated = current.disableSensor(sensorType);
            }

            addPendingOp(PendingOp.SENSOR_CONF);
            wearableDevice.changeSensorConfiguration(updated);
        }
    }

    public boolean sensorEnabled(@NonNull final SensorType sensorType) {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            return wearableDevice.sensorConfiguration()
                .sensorIsEnabled(sensorType);
        } else {
            return false;
        }
    }

    public LiveData<SensorValue> accelerometerData() {
        return mAccelerometerData;
    }

    public LiveData<SensorValue> gyroscopeData() {
        return mGyroscopeData;
    }

    public LiveData<SensorValue> rotationVectorData() {
        return mRotationVectorData;
    }

    public LiveData<SensorValue> gameRotationData() {
        return mGameRotationData;
    }

    public LiveData<SensorValue> orientationData() {
        return mOrientationData;
    }

    public LiveData<SensorValue> magnetometerData() {
        return mMagnetometerData;
    }

    public LiveData<SensorValue> uncalibratedMagnetometerData() {
        return mUncalibratedMagnetometerData;
    }

    public LiveData<Event<DeviceException>> errors() {
        return mErrors;
    }

    public LiveData<Boolean> busy() {
        return mBusy;
    }

    public LiveData<Event<?>> focusReceived() {
        return mFocusReceived;
    }

    public LiveData<Boolean> focusRequired() {
        return Transformations.map(LiveDataUtil.distinctUntilChanged(mFocus),
            haveFocus -> !haveFocus);
    }

    public LiveData<Boolean> sensorsSuspended() {
        return mSensorsSuspended;
    }

    // Gestures

    public void refreshGestureConfiguration() {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            addPendingOp(PendingOp.GESTURE_CONF);
            wearableDevice.refreshGestureConfiguration();
        }
    }

    @NonNull
    public LiveData<GestureConfiguration> wearableGestureConfiguration() {
        return mWearableGestureConfiguration;
    }

    public void changeGestureConfiguration(@NonNull final GestureConfiguration gestureConfiguration) {
        final WearableDevice wearableDevice = device();
        if (wearableDevice != null) {
            addPendingOp(PendingOp.GESTURE_CONF);
            wearableDevice.changeGestureConfiguration(gestureConfiguration);
        }
    }

    public List<GestureEvent> gestures() {
        return mGestureEvents;
    }

    public void clearGestures() {
        mGestureEvents.clear();
    }

    public LiveData<Event<GestureEvent>> gestureEvents() {
        return _mGestureEvents;
    }

    // WearableDeviceListener implementation

    @Override
    public void onFocusLost() {
        mFocus.setValue(false);
    }

    @Override
    public void onFocusGained() {
        mFocus.setValue(true);
    }

    @Override
    public void onFocusRequestSucceeded() {
        // We'll wait for onFocusGained()
    }

    @Override
    public void onFocusRequestFailed(@NonNull final BoseWearableException wearableException) {
        mErrors.setValue(new Event<>(wearableException));
    }

    @Override
    public void onSensorsSuspended(@NonNull final SensorsSuspensionReason suspensionReason) {
        mSensorsSuspended.setValue(true);
    }

    @Override
    public void onSensorsResumed() {
        mSensorsSuspended.setValue(false);
    }

    @Override
    public void onDeviceInfoRead(@NonNull final DeviceInformation deviceInformation) {
        mDeviceInfo.setValue(deviceInformation);
    }

    @Override
    public void onWearableDeviceInfoRead(@NonNull final WearableDeviceInformation deviceInformation) {
        pendingOpMaybeDone(PendingOp.WEARABLE_DEVICE_INFO);
        mWearableDeviceInfo.setValue(deviceInformation);
    }

    @Override
    public void onSensorConfigurationChanged(@NonNull final SensorConfiguration sensorConfiguration) {
        // We'll wait for onSensorConfigurationRead()
    }

    @Override
    public void onSensorConfigurationError(@NonNull final BoseWearableException wearableException) {
        pendingOpMaybeDone(PendingOp.SENSOR_CONF);
        mErrors.setValue(new Event<>(wearableException));
    }

    @Override
    public void onSensorInfoRead(@NonNull final SensorInformation sensorInformation) {
        pendingOpMaybeDone(PendingOp.SENSOR_INFO);
        mWearableSensorInfo.setValue(sensorInformation);
    }

    @Override
    public void onSensorConfigurationRead(@NonNull final SensorConfiguration sensorConfiguration) {
        pendingOpMaybeDone(PendingOp.SENSOR_CONF);
        mWearableSensorConfiguration.setValue(sensorConfiguration);
    }

    @Override
    public void onGestureConfigurationChanged(@NonNull final GestureConfiguration gestureConfiguration) {
        // We'll wait for onGestureConfigurationRead()
    }

    @Override
    public void onGestureConfigurationError(@NonNull final BoseWearableException wearableException) {
        pendingOpMaybeDone(PendingOp.GESTURE_CONF);
        mErrors.setValue(new Event<>(wearableException));
    }

    @Override
    public void onSensorDataRead(@NonNull final SensorValue value) {
        switch (value.sensorType()) {
            case ACCELEROMETER:
                mAccelerometerData.setValue(value);
                break;
            case GYROSCOPE:
                mGyroscopeData.setValue(value);
                break;
            case ROTATION_VECTOR:
                mRotationVectorData.setValue(value);
                break;
            case GAME_ROTATION_VECTOR:
                mGameRotationData.setValue(value);
                break;
            case ORIENTATION:
                mOrientationData.setValue(value);
                break;
            case MAGNETOMETER:
                mMagnetometerData.setValue(value);
                break;
            case UNCALIBRATED_MAGNETOMETER:
                mUncalibratedMagnetometerData.setValue(value);
                break;
        }
    }

    @Override
    public void onGestureConfigurationRead(@NonNull final GestureConfiguration gestureConfiguration) {
        pendingOpMaybeDone(PendingOp.GESTURE_CONF);
        mWearableGestureConfiguration.setValue(gestureConfiguration);
    }

    @Override
    public void onGestureDataRead(@NonNull final GestureData gestureData) {
        final GestureEvent event = new GestureEvent(gestureData);
        mGestureEvents.add(event);
        _mGestureEvents.setValue(new Event<>(event));
    }

    @Override
    public void onDevicePropertiesRead(@NonNull final DeviceProperties deviceProperties) {
        mDeviceProperties.setValue(deviceProperties);
    }

    private void stopSession() {
        final ConnectionState state = mConnectionState.getValue();
        if (state instanceof ConnectionState.Idle) {
            return;
        }

        mConnectionState.setValue(ConnectionState.IDLE);
        mSensorsSuspended.setValue(false);

        final Session session;
        final WearableDevice device;
        if (state instanceof ConnectionState.Connecting) {
            session = ((ConnectionState.Connecting) state).session();
            device = null;
        } else if (state instanceof ConnectionState.BondingRequired) {
            session = ((ConnectionState.BondingRequired) state).session();
            device = null;
        } else if (state instanceof ConnectionState.Connected) {
            session = ((ConnectionState.Connected) state).session();
            device = ((ConnectionState.Connected) state).device();
        } else {
            session = null;
            device = null;
        }

        if (device != null) {
            device.removeListener(this);
        }

        if (session != null) {
            session.callback(null);
            session.close();

            BoseWearable.getInstance()
                .bluetoothManager()
                .removeSession(session);
        }
    }

    private DeviceException parseStatusCode(final int statusCode) {
        switch (statusCode) {
            case 0:
                return null;
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

    @Nullable
    private Destination currentDestination() {
        final ConnectionState state = mConnectionState.getValue();
        if (state instanceof ConnectionState.Connecting) {
            return ((ConnectionState.Connecting) state).destination();
        } else if (state instanceof ConnectionState.BondingRequired) {
            return ((ConnectionState.BondingRequired) state).destination();
        } else if (state instanceof ConnectionState.Connected) {
            return ((ConnectionState.Connected) state).destination();
        }
        return null;
    }

    @Nullable
    private WearableDevice device() {
        final ConnectionState state = mConnectionState.getValue();
        if (state instanceof ConnectionState.Connected) {
            return ((ConnectionState.Connected) state).device();
        } else {
            Log.w(TAG, "Session not open");
            return null;
        }
    }

    private enum PendingOp {
        CONNECTING,
        REFRESH_SERVICES,
        WEARABLE_DEVICE_INFO,
        SENSOR_INFO,
        SENSOR_CONF,
        GESTURE_CONF,
    }

    private void addPendingOp(@NonNull final PendingOp op) {
        final boolean wasEmpty = mPendingOps.isEmpty();
        mPendingOps.add(op);
        if (wasEmpty) {
            mBusy.setValue(true);
        }
    }

    private void pendingOpMaybeDone(@NonNull final PendingOp op) {
        if (op.equals(mPendingOps.peek())) {
            mPendingOps.remove();
            if (mPendingOps.isEmpty()) {
                mBusy.setValue(false);
            }
        }
    }
}
