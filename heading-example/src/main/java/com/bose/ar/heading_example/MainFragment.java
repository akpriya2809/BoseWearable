package com.bose.ar.heading_example;

//
//  MainFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 02/19/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bose.blecore.DeviceException;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class MainFragment extends Fragment {
    public static final String ARG_DEVICE_ADDRESS = "device-address";
    public static final String ARG_USE_SIMULATED_DEVICE = "use-simulated-device";
    private static final String TAG = MainFragment.class.getSimpleName();

    private String mDeviceAddress;
    private boolean mUseSimulatedDevice;
    @SuppressWarnings("PMD.SingularField") // Need to keep a reference to it so it does not get GC'd
    private MainViewModel mViewModel;
    private View mParentView;
    @Nullable
    private ProgressBar mProgressBar;
    private SwitchCompat mTrueNorthSwitch;
    private TextView mHeadingText;
    private TextView mAccuracyText;
    @Nullable
    private Snackbar mSnackBar;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mDeviceAddress = args.getString(ARG_DEVICE_ADDRESS);
            mUseSimulatedDevice = args.getBoolean(ARG_USE_SIMULATED_DEVICE, false);
        }

        if (mDeviceAddress == null && !mUseSimulatedDevice) {
            throw new IllegalArgumentException();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mParentView = view.findViewById(R.id.container);

        mTrueNorthSwitch = view.findViewById(R.id.trueNorthSwitch);
        mHeadingText = view.findViewById(R.id.headingText);
        mAccuracyText = view.findViewById(R.id.accuracyText);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = requireActivity();
        mProgressBar = activity.findViewById(R.id.progressbar);

        mViewModel = ViewModelProviders.of(this)
            .get(MainViewModel.class);

        mViewModel.busy()
            .observe(this, this::onBusy);

        mViewModel.errors()
            .observe(this, this::onError);

        mViewModel.sensorsSuspended()
            .observe(this, this::onSensorsSuspended);

        mViewModel.heading()
            .observe(this, this::onHeadingUpdated);

        mViewModel.accuracy()
            .observe(this, this::onAccuracyUpdated);

        mTrueNorthSwitch.setChecked(mViewModel.useTrueNorth());
        mTrueNorthSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.useTrueNorth(isChecked);
        });

        if (mDeviceAddress != null) {
            mViewModel.selectDevice(mDeviceAddress);
        } else if (mUseSimulatedDevice) {
            mViewModel.selectSimulatedDevice();
        }
    }

    @Override
    public void onDestroy() {
        onBusy(false);

        final Snackbar snackbar = mSnackBar;
        mSnackBar = null;
        if (snackbar != null) {
            snackbar.dismiss();
        }

        super.onDestroy();
    }

    private void onBusy(final boolean isBusy) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(isBusy ? View.VISIBLE : View.INVISIBLE);
        }

        final Activity activity = getActivity();
        final Window window = activity != null ? activity.getWindow() : null;
        if (window != null) {
            if (isBusy) {
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }

    private void onError(@NonNull final Event<DeviceException> event) {
        final DeviceException deviceException = event.get();
        if (deviceException != null) {
            showError(deviceException.getMessage());
            getFragmentManager().popBackStack();
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

    private void onHeadingUpdated(final double heading) {
        mHeadingText.setText(formatAngle(heading));
    }

    private void onAccuracyUpdated(final double accuracy) {
        mAccuracyText.setText(formatAngle(accuracy));
    }

    private void showError(final String message) {
        final Context context = getContext();
        if (context != null) {
            final Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Log.e(TAG, "Device error: " + message);
        }
    }

    private String formatAngle(final double angle) {
        return String.format(Locale.US, "%.2f°", angle);
    }
}
