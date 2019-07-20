package com.bose.wearable.sample;

//
//  MainActivity.java
//  BoseWearable
//
//  Created by Tambet Ingo on 09/27/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bose.blecore.DeviceException;
import com.bose.blecore.MissingCharacteristicException;
import com.bose.wearable.BoseWearableException;
import com.bose.wearable.WearableDeviceException;
import com.bose.wearable.sample.viewmodels.ConnectionState;
import com.bose.wearable.sample.viewmodels.Destination;
import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    static final String ARG_DEVICE_ADDRESS = "device-address";
    static final String ARG_SIMULATED_DEVICE = "simulated-device";

    @SuppressWarnings("PMD.SingularField") // Need to keep a reference to it so it does not get GC'd
    private SessionViewModel mViewModel;
    private Toolbar mToolbar;
    private View mParentView;
    private ProgressBar mProgressBar;
    private ViewGroup mContent;
    @Nullable
    private Snackbar mSnackBar;
    private boolean mStopOnDisconnect;

    public static Intent intentForDevice(@NonNull final Context context,
                                         @NonNull final String deviceAddress) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_DEVICE_ADDRESS, deviceAddress);
        return intent;
    }

    public static Intent intentForSimulatedDevice(@NonNull final Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_SIMULATED_DEVICE, true);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mParentView = findViewById(R.id.container);
        mProgressBar = findViewById(R.id.progressbar);
        mContent = findViewById(R.id.my_nav_host_fragment);

        mViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);
        mViewModel.monitorConnection()
            .observe(this, this::onConnectedChanged);

        mViewModel.busy()
            .observe(this, this::onBusy);

        mViewModel.errors()
            .observe(this, this::onError);

        mViewModel.focusReceived()
            .observe(this, this::onFocusReceived);

        mViewModel.focusRequired()
            .observe(this, this::onFocusRequired);

        mViewModel.sensorsSuspended()
            .observe(this, this::onSensorsSuspended);

        final Intent intent = getIntent();
        final String deviceAddress = intent.getStringExtra(ARG_DEVICE_ADDRESS);
        final boolean simulatedDevice = intent.getBooleanExtra(ARG_SIMULATED_DEVICE, false);

        final Destination destination;
        if (deviceAddress != null) {
            destination = Destination.device(deviceAddress);
        } else if (simulatedDevice) {
            destination = Destination.simulated();
        } else {
            throw new IllegalArgumentException();
        }

        if (destination != null) {
            mViewModel.connect(destination);
        } else {
            showError(getString(R.string.wearable_error_device_not_found), ErrorType.FATAL);
        }

        final NavController navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.my_nav_host_fragment)
            .navigateUp();
    }

    @Override
    public void setTitle(final CharSequence title) {
        mToolbar.setTitle(title);
    }

    private void onConnectedChanged(@NonNull final ConnectionState state) {
        final Destination destination;
        if (state instanceof ConnectionState.Connecting) {
            destination = ((ConnectionState.Connecting) state).destination();
            mStopOnDisconnect = true;
        } else if (state instanceof ConnectionState.Connected) {
            destination = ((ConnectionState.Connected) state).destination();
            mStopOnDisconnect = true;
        } else {
            if (mStopOnDisconnect) {
                finish();
                return;
            }

            destination = null;
        }

        if (destination != null) {
            setTitle(destination.name());
        }
    }

    private void onBusy(final boolean isBusy) {
        mProgressBar.setVisibility(isBusy ? View.VISIBLE : View.INVISIBLE);
        viewsEnabled(mContent, !isBusy);
    }

    private void onFocusReceived(final Event<?> event) {
        if (event.get() != null) {
            Snackbar.make(mParentView, R.string.focus_received, Snackbar.LENGTH_SHORT)
                .show();
        }
    }

    private void onFocusRequired(final boolean focusRequired) {
        if (focusRequired) {
            final Snackbar snackbar = Snackbar.make(mParentView, R.string.focus_lost,
                Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction(R.string.focus_request, v -> mViewModel.requestFocus());
            snackbar.show();
        }

        viewsEnabled(mContent, !focusRequired);
    }

    private void onSensorsSuspended(final boolean isSuspended) {
        final Snackbar snackbar;
        if (isSuspended) {
            snackbar = Snackbar.make(mParentView, R.string.sensors_suspended,
                Snackbar.LENGTH_INDEFINITE);
        } else if (mSnackBar != null) {
            snackbar = Snackbar.make(mParentView, R.string.sensors_resumed,
                Snackbar.LENGTH_SHORT);
        } else {
            snackbar = null;
        }

        if (snackbar != null) {
            snackbar.show();
        }

        mSnackBar = snackbar;
    }

    private void viewsEnabled(final ViewGroup viewGroup,
                              final boolean enabled) {
        viewGroup.setEnabled(enabled);

        final int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                viewsEnabled((ViewGroup) view, enabled);
            }
        }
    }

    private void onError(@NonNull final Event<DeviceException> event) {
        final DeviceException exception = event.get();
        if (exception == null) {
            return;
        }

        final String msg;
        ErrorType errorType = ErrorType.FATAL;

        if (exception instanceof WearableDeviceException) {
            errorType = ErrorType.WARNING;
            switch (exception.code()) {
                case WearableDeviceException.INVALID_REQUEST_LENGTH:
                    msg = getString(R.string.wearable_error_invalid_request_length);
                    break;
                case WearableDeviceException.INVALID_SAMPLE_PERIOD:
                    msg = getString(R.string.wearable_error_invalid_sample_period);
                    break;
                case WearableDeviceException.INVALID_SENSOR_CONFIGURATION:
                    msg = getString(R.string.wearable_error_invalid_sensor_configuration);
                    break;
                case WearableDeviceException.CONFIG_EXCEEDS_MAX_THROUGHPUT:
                    msg = getString(R.string.wearable_error_config_exceeds_max_throughput);
                    break;
                case WearableDeviceException.WEARABLE_SENSOR_SERVICE_UNAVAILABLE:
                    msg = getString(R.string.wearable_error_wearable_sensor_service_unavailable);
                    break;
                case WearableDeviceException.INVALID_SENSOR:
                    msg = getString(R.string.wearable_error_invalid_sensor);
                    break;
                case WearableDeviceException.TIMEOUT:
                    msg = getString(R.string.wearable_error_timeout);
                    break;
                case WearableDeviceException.UNKNOWN:
                default:
                    msg = getString(R.string.wearable_error_unknown);
                    break;
            }
        } else if (exception instanceof BoseWearableException) {
            switch (exception.code()) {
                case BoseWearableException.INVALID_RESPONSE:
                    msg = getString(R.string.wearable_error_invalid_response);
                    errorType = ErrorType.WARNING;
                    break;
                case BoseWearableException.DEVICE_OUT_OF_RANGE:
                    msg = getString(R.string.wearable_error_device_out_of_range);
                    break;
                case BoseWearableException.DEVICE_NOT_FOUND:
                    msg = getString(R.string.wearable_error_device_not_found);
                    break;
                default:
                    msg = getString(R.string.wearable_error_unknown);
                    break;
            }
        } else if (exception instanceof MissingCharacteristicException) {
            final MissingCharacteristicException mce = (MissingCharacteristicException) exception;
            msg = getString(R.string.device_error_missing_characteristic,
                mce.service().toString(), mce.characteristic().toString());
        } else {
            switch (exception.code()) {
                case DeviceException.FIRMWARE_UPDATE_REQUIRED:
                    msg = getString(R.string.device_error_firmware_update_required);
                    break;
                case DeviceException.UNSUPPORTED_DEVICE:
                    msg = getString(R.string.device_error_unsupported_device);
                    break;
                case DeviceException.NO_MATCHING_DEVICE_TYPES_FOUND:
                    msg = getString(R.string.device_error_no_device_types_found);
                    break;
                case DeviceException.DISCONNECTED_BY_DEVICE:
                    msg = getString(R.string.wearable_error_disconnected_by_device);
                    break;
                case DeviceException.REFRESH_REQUIRED:
                    msg = getString(R.string.device_error_refresh_required);
                    break;
                default:
                    msg = getString(R.string.wearable_error_unknown);
                    break;
            }
        }

        showError(msg, errorType);
    }

    private void showError(@NonNull final String message,
                           @NonNull final ErrorType errorType) {
        Toast.makeText(this, getString(R.string.wearable_error_format, message), Toast.LENGTH_LONG)
            .show();

        if (errorType == ErrorType.FATAL) {
            finish();
        }
    }

    private enum ErrorType {
        WARNING,
        FATAL
    }
}
