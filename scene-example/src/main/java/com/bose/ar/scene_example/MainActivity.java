package com.bose.ar.scene_example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bose.blecore.DeviceException;
import com.bose.scene_example.R;
import com.bose.wearable.BoseWearableException;
import com.bose.wearable.WearableDeviceException;
import com.bose.wearable.services.wearablesensor.SensorType;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements ErrorDisplay {
    static final String ARG_SENSOR_TYPE = "sensor-type";
    static final String ARG_DEVICE_ADDRESS = "device-address";
    static final String ARG_SIMULATED_DEVICE = "simulated-device";

    private View mParentView;
    private ProgressBar mProgressBar;
    private SensorViewModel mViewModel;
    @Nullable
    private Snackbar mSnackBar;

    public static Intent intentForDevice(@NonNull final Context context,
                                         @NonNull final String deviceAddress,
                                         @NonNull final SensorType sensorType) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_DEVICE_ADDRESS, deviceAddress);
        intent.putExtra(ARG_SENSOR_TYPE, sensorType);
        return intent;
    }

    public static Intent intentForSimulatedDevice(@NonNull final Context context,
                                                  @NonNull final SensorType sensorType) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_SENSOR_TYPE, sensorType);
        intent.putExtra(ARG_SIMULATED_DEVICE, true);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String deviceAddress = intent.getStringExtra(ARG_DEVICE_ADDRESS);
        final SensorType sensorType = (SensorType) intent.getSerializableExtra(ARG_SENSOR_TYPE);
        final boolean simulatedDevice = intent.getBooleanExtra(ARG_SIMULATED_DEVICE, false);

        if (sensorType == null || deviceAddress == null && !simulatedDevice) {
            throw new IllegalArgumentException();
        }

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mParentView = findViewById(R.id.container);
        mProgressBar = findViewById(R.id.progressbar);

        mViewModel = ViewModelProviders.of(this).get(SensorViewModel.class);
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

        mViewModel.sensorType(sensorType);

        final Destination destination;
        if (deviceAddress != null) {
            destination = Destination.device(deviceAddress);
        } else {
            destination = Destination.simulated();
        }
        if (destination != null) {
            mViewModel.connect(destination);
        } else {
            showError(getString(R.string.device_not_found, deviceAddress));
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
    public void showWarning(@NonNull final String message) {
        final Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void showError(@NonNull final String message) {
        showWarning(message);
        finish();
    }

    private void onBusy(final boolean isBusy) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(isBusy ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void onFocusReceived(final Event<?> event) {
        if (event.get() != null) {
            Snackbar.make(mParentView, R.string.focus_recevied, Snackbar.LENGTH_SHORT)
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
    }

    private void onError(@NonNull final Event<DeviceException> event) {
        final DeviceException deviceException = event.get();
        if (deviceException == null) {
            return;
        }

        if (isFatalError(deviceException)) {
            showError(deviceException.getMessage());
        } else {
            showWarning(deviceException.getMessage());
        }
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

    private static boolean isFatalError(@NonNull final DeviceException deviceException) {
        if (deviceException instanceof WearableDeviceException) {
            return false;
        }
        if (deviceException instanceof BoseWearableException) {
            switch (deviceException.code()) {
                case BoseWearableException.INVALID_RESPONSE:
                    return false;
            }
        }

        return true;
    }
}
