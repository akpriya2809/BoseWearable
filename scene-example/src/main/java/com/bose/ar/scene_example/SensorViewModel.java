package com.bose.ar.scene_example;

//
//  SensorViewModel.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/19/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.bose.ar.scene_example.completable.CompletableWearableDevice;
import com.bose.blecore.DeviceException;
import com.bose.blecore.Session;
import com.bose.blecore.SessionDelegate;
import com.bose.wearable.BoseWearable;
import com.bose.wearable.BoseWearableException;
import com.bose.wearable.WearableDeviceException;
import com.bose.wearable.focus.FocusMode;
import com.bose.wearable.sensordata.GestureData;
import com.bose.wearable.sensordata.GestureIntent;
import com.bose.wearable.sensordata.QuaternionAccuracy;
import com.bose.wearable.sensordata.SensorIntent;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.services.wearablesensor.GestureConfiguration;
import com.bose.wearable.services.wearablesensor.GestureType;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorConfiguration;
import com.bose.wearable.services.wearablesensor.SensorType;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;
import com.bose.wearable.wearabledevice.BaseWearableDeviceListener;
import com.bose.wearable.wearabledevice.WearableDeviceListener;
import com.google.ar.sceneform.math.Quaternion;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class SensorViewModel extends ViewModel {
    private static final String TAG = SensorViewModel.class.getSimpleName();
    private static final SamplePeriod SAMPLE_PERIOD = SamplePeriod._40_MS;
    private static final GestureType GESTURE_TYPE = GestureType.INPUT;

    @NonNull
    private final MutableLiveData<ConnectionState> mConnectionState = new MutableLiveData<>();
    @NonNull
    private final ARCoreSensorValueReader mValueReader = new ARCoreSensorValueReader();
    @NonNull
    private final MutableLiveData<Boolean> mBusy = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Event<DeviceException>> mErrors = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> mFocus = new MutableLiveData<>();
    @NonNull
    private final LiveData<Event<?>> mFocusReceived;
    private final SingleMediatorLiveData<Boolean> mSuspendedMediator = new SingleMediatorLiveData<>();
    @NonNull
    private final MutableLiveData<WearableDeviceInformation> mWearableDeviceInfo = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Quaternion> mSensorData = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<QuaternionAccuracy> mSensorAccuracy = new MutableLiveData<>();
    @NonNull
    private SensorType mSensorType = SensorType.ROTATION_VECTOR;

    public SensorViewModel() {
        mConnectionState.setValue(ConnectionState.IDLE);

        setupSensorMonitoring(mFocus);
        setupFocusMonitoring(mConnectionState);
        setupDeviceTypeMonitoring(mConnectionState);

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

    public void sensorType(@NonNull final SensorType sensorType) {
        if (sensorType != SensorType.ROTATION_VECTOR &&
            sensorType != SensorType.GAME_ROTATION_VECTOR) {
            throw new IllegalArgumentException("Only Rotation Vector and Game Rotation Vector sensor are supported");
        }

        mSensorType = sensorType;
    }

    public void correctedInitially(final boolean value) {
        mValueReader.correctedInitially(value);
    }

    public void inverted(final boolean value) {
        mValueReader.inverted(value);
    }

    public boolean inverted() {
        return mValueReader.inverted();
    }

    public void resetInitialReading() {
        mValueReader.resetInitialReading();
    }

    public LiveData<Boolean> busy() {
        return mBusy;
    }

    public LiveData<Event<DeviceException>> errors() {
        return mErrors;
    }

    public LiveData<Event<?>> focusReceived() {
        return mFocusReceived;
    }

    public LiveData<Boolean> focusRequired() {
        if (BoseWearable.getInstance().focusMode() == FocusMode.MANUAL) {
            return Transformations.map(LiveDataUtil.distinctUntilChanged(mFocus),
                haveFocus -> !haveFocus);
        } else {
            return new NeverLiveData<>();
        }
    }

    public LiveData<Boolean> sensorsSuspended() {
        return mSuspendedMediator;
    }

    public LiveData<WearableDeviceInformation> wearableDeviceInfo() {
        return mWearableDeviceInfo;
    }

    public LiveData<Quaternion> sensorData() {
        return mSensorData;
    }

    public LiveData<QuaternionAccuracy> sensorAccuracy() {
        return mSensorAccuracy;
    }

    public static SensorIntent sensorIntent(@NonNull final SensorType sensorType) {
        return new SensorIntent(Collections.singleton(sensorType), Collections.singleton(SAMPLE_PERIOD));
    }

    public static GestureIntent gestureIntent() {
        return new GestureIntent(Collections.singleton(GESTURE_TYPE));
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
                mBusy.setValue(false);
                mConnectionState.setValue(new ConnectionState.Connected(destination, session));
            }

            @Override
            public void sessionClosed(final int statusCode) {
                if (statusCode == 0) {
                    // Closed normally
                    mBusy.setValue(false);
                    stopSession();
                    resetInitialReading();
                } else {
                    sessionError(parseStatusCode(statusCode));
                }
            }

            @Override
            public void sessionError(@NonNull final DeviceException exception) {
                mBusy.setValue(false);
                stopSession();
                resetInitialReading();

                mErrors.setValue(new Event<>(exception));
            }
        });

        mConnectionState.setValue(new ConnectionState.Connecting(destination, session));
        mBusy.setValue(true);

        session.open();
    }

    public void requestFocus() {
        final CompletableWearableDevice device = deviceFromState();
        device.requestFocus()
            .handle((__, throwable) -> {
                mFocus.setValue(throwable == null);
                if (throwable instanceof DeviceException) {
                    mErrors.setValue(new Event<>((DeviceException) throwable));
                }
                return null;
            });
    }

    private void setupSensorMonitoring(@NonNull final LiveData<Boolean> focus) {
        final WearableDeviceListener listener = new BaseWearableDeviceListener() {
            @Override
            public void onSensorDataRead(@NonNull final SensorValue sensorData) {
                if (mSensorType.equals(sensorData.sensorType())) {
                    onSensorData(sensorData);
                }
            }

            @Override
            public void onGestureDataRead(@NonNull final GestureData gestureData) {
                if (GESTURE_TYPE.equals(gestureData.type())) {
                    onGestureData();
                }
            }
        };

        focus.observeForever(new Observer<Boolean>() {
            private boolean mHaveFocus;
            private CompletableWearableDevice mDevice;

            @Override
            public void onChanged(final Boolean haveFocus) {
                if (mHaveFocus == haveFocus) {
                    return;
                }
                mHaveFocus = haveFocus;

                if (haveFocus) {
                    mDevice = deviceFromState();
                    mDevice.addListener(listener);
                    startSensors(mDevice);
                } else if (mDevice != null) {
                    mDevice.removeListener(listener);
                    mDevice = null;
                }
            }
        });
    }

    private void setupFocusMonitoring(@NonNull final LiveData<ConnectionState> connectionState) {
        final WearableDeviceListener listener = new BaseWearableDeviceListener() {
            @Override
            public void onFocusLost() {
                mFocus.setValue(false);
            }

            @Override
            public void onFocusGained() {
                mFocus.setValue(true);
            }
        };

        connectionState.observeForever(new Observer<ConnectionState>() {
            @Nullable
            private CompletableWearableDevice mDevice;

            @Override
            public void onChanged(final ConnectionState state) {
                if (mDevice != null) {
                    mDevice.removeListener(listener);
                    mDevice = null;
                }

                if (state instanceof ConnectionState.Connected) {
                    mDevice = ((ConnectionState.Connected) state).device();
                    mDevice.addListener(listener);
                    mFocus.setValue(mDevice.hasFocus());
                } else {
                    mFocus.setValue(false);
                }
            }
        });
    }

    private void setupDeviceTypeMonitoring(@NonNull final LiveData<ConnectionState> connectionState) {
        connectionState.observeForever(state -> {
            if (state instanceof ConnectionState.Connected) {
                final CompletableWearableDevice device = ((ConnectionState.Connected) state).device();
                mWearableDeviceInfo.setValue(device.wearableDeviceInformation());
                mSuspendedMediator.source(device.monitorSuspended());
            } else {
                if (mWearableDeviceInfo.getValue() != null) {
                    mWearableDeviceInfo.setValue(null);
                }
                mSuspendedMediator.source(null);
            }
        });
    }

    private void startSensors(final CompletableWearableDevice device) {
        mBusy.setValue(true);
        startDevice(device)
                .thenRun(() -> mBusy.setValue(false))
                .handle((aVoid, throwable) -> {
                    final Throwable cause = throwable.getCause();
                    if (cause instanceof DeviceException) {
                        mErrors.setValue(new Event<>((DeviceException) cause));
                    }
                    mBusy.setValue(false);
                    return null;
                });
    }

    private void onSensorData(@NonNull final SensorValue sensorValue) {
        final Quaternion quaternion = mValueReader.quaternion(sensorValue);
        if (quaternion != null) {
            mSensorData.setValue(quaternion);
        }

        final QuaternionAccuracy accuracy = sensorValue.quaternionAccuracy();
        if (accuracy != null) {
            mSensorAccuracy.setValue(accuracy);
        }
    }

    private void onGestureData() {
        mValueReader.resetInitialReading();
    }

    private CompletableFuture<Void> startDevice(@NonNull final CompletableWearableDevice wearableDevice) {
        final SensorConfiguration sensorConf = wearableDevice.sensorConfiguration()
            .disableAll()
            .enableSensor(mSensorType, SAMPLE_PERIOD);

        final CompletableFuture<?> enableSensorsFuture = wearableDevice.changeSensors(sensorConf);

        final GestureConfiguration gestureConf = wearableDevice.gestureConfiguration()
            .disableAll()
            .gestureEnabled(GESTURE_TYPE, true);

        final CompletableFuture<?> enableGesturesFuture = wearableDevice.changeGestures(gestureConf)
            .handle((conf, throwable) -> {
                if (throwable != null) {
                    Log.e(TAG, "Could not enable gestures: " + throwable.getMessage());
                }

                return conf;
            });

        return CompletableFuture.allOf(enableSensorsFuture, enableGesturesFuture);
    }

    private CompletableFuture<Void> stopDevice(@NonNull final CompletableWearableDevice wearableDevice) {
        final SensorConfiguration sensorConf = wearableDevice.sensorConfiguration()
            .disableAll();
        final CompletableFuture<?> disableSensorsFuture = wearableDevice.changeSensors(sensorConf)
            .handle((conf, throwable) -> null);

        final GestureConfiguration gestureConf = wearableDevice.gestureConfiguration()
            .disableAll();
        final CompletableFuture<?> disableGesturesFuture = wearableDevice.changeGestures(gestureConf)
            .handle((conf, throwable) -> null);

        return CompletableFuture.allOf(disableSensorsFuture, disableGesturesFuture);
    }

    private void stopSession() {
        final ConnectionState state = mConnectionState.getValue();

        if (state instanceof ConnectionState.Idle) {
            return;
        }

        mConnectionState.setValue(ConnectionState.IDLE);

        if (state instanceof ConnectionState.Connecting) {
            closeSession(((ConnectionState.Connecting) state).session());
            return;
        }

        if (!(state instanceof ConnectionState.Connected)) {
            return;
        }

        final ConnectionState.Connected connectedState = (ConnectionState.Connected) state;
        final Session session = connectedState.session();
        final CompletableWearableDevice wearableDevice = connectedState.device();
        final CompletableFuture<Void> future;

        if (session.device() != null) {
            future = stopDevice(wearableDevice)
                .whenComplete((aVoid, throwable) -> {
                    mSensorData.setValue(null);
                    mSensorAccuracy.setValue(null);
                });
        } else {
            future = CompletableFuture.completedFuture(null);
        }

        future.whenComplete((aVoid, throwable) -> closeSession(session));
    }

    private void closeSession(@NonNull final Session session) {
        session.callback(null);
        session.close();

        BoseWearable.getInstance()
            .bluetoothManager()
            .removeSession(session);
    }

    @Nullable
    private Destination currentDestination() {
        final ConnectionState state = mConnectionState.getValue();
        if (state instanceof ConnectionState.Connecting) {
            return ((ConnectionState.Connecting) state).destination();
        } else if (state instanceof ConnectionState.Connected) {
            return ((ConnectionState.Connected) state).destination();
        }
        return null;
    }

    private CompletableWearableDevice deviceFromState() {
        final ConnectionState state = mConnectionState.getValue();
        if (!(state instanceof ConnectionState.Connected)) {
            throw new IllegalStateException("Not connected to device");
        }

        return ((ConnectionState.Connected) state).device();
    }

    @NonNull
    private DeviceException parseStatusCode(final int statusCode) {
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

    private static class SingleMediatorLiveData<T> extends MediatorLiveData<T> {
        @Nullable
        private LiveData<T> mSource;

        void source(@Nullable final LiveData<T> source) {
            if (mSource != null) {
                removeSource(mSource);
            }
            mSource = source;
            if (mSource != null) {
                addSource(mSource, this::setValue);
            }
        }
    }
}
