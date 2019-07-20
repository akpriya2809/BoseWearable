package com.bose.wearable.sample;

//
//  HomeFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/03/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bose.blecore.Logger;
import com.bose.blecore.ScanError;
import com.bose.bosewearableui.DeviceConnectorActivity;
import com.bose.wearable.sensordata.GestureIntent;
import com.bose.wearable.sensordata.SensorIntent;

public class HomeFragment extends Fragment {
    private static final int REQUEST_CODE_CONNECTOR = 1;
    private static final int AUTO_CONNECT_TIMEOUT = 5;
    private static final String PREF_AUTO_CONNECT_ENABLED = "auto-connect-enabled";

    private SwitchCompat mAutoConnectSwitch;

    interface Listener {
        void onDeviceSelected(@NonNull String deviceAddress);
        void onSimulatedDeviceSelected();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final Button searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> onSearchClicked());

        mAutoConnectSwitch = view.findViewById(R.id.autoConnectSwitch);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mAutoConnectSwitch.setChecked(prefs.getBoolean(PREF_AUTO_CONNECT_ENABLED, true));
        mAutoConnectSwitch.setOnCheckedChangeListener((compoundButton, enabled) -> {
            prefs.edit()
                    .putBoolean(PREF_AUTO_CONNECT_ENABLED, enabled)
                    .apply();
        });

        final Button simulatedDeviceButton = view.findViewById(R.id.simulatedDeviceButton);
        simulatedDeviceButton.setOnClickListener(v -> onSimulatedDeviceClicked());

        final TextView versionText = view.findViewById(R.id.versionText);
        versionText.setText(getString(R.string.version_name, BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE));

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CONNECTOR:
                if (resultCode == Activity.RESULT_OK) {
                    final String deviceAddress = data != null ? data.getStringExtra(DeviceConnectorActivity.CONNECTED_DEVICE) : null;
                    if (deviceAddress != null) {
                        onDeviceSelected(deviceAddress);
                    } else {
                        showNoDeviceError();
                    }
                } else if (resultCode == DeviceConnectorActivity.RESULT_SCAN_ERROR) {
                    final ScanError scanError = (ScanError) data.getSerializableExtra(DeviceConnectorActivity.FAILURE_REASON);
                    showScanError(scanError);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void onSearchClicked() {
        final int autoConnectTimeout = mAutoConnectSwitch.isChecked() ? AUTO_CONNECT_TIMEOUT : 0;
        final Intent intent = DeviceConnectorActivity.newIntent(requireContext(), autoConnectTimeout,
            SensorIntent.EMPTY, GestureIntent.EMPTY);

        startActivityForResult(intent, REQUEST_CODE_CONNECTOR);
    }

    private void onSimulatedDeviceClicked() {
        final Activity activity = getActivity();
        if (activity instanceof Listener) {
            final Listener listener = (Listener) activity;
            listener.onSimulatedDeviceSelected();
        }
    }

    private void onDeviceSelected(@NonNull final String deviceAddress) {
        final Activity activity = getActivity();
        if (activity instanceof Listener) {
            final Listener listener = (Listener) activity;
            listener.onDeviceSelected(deviceAddress);
        }
    }

    private void showNoDeviceError() {
        final Context context = getContext();
        if (context != null) {
            Toast.makeText(context, getString(R.string.no_device_selected),
                Toast.LENGTH_LONG)
                .show();
        }
    }

    private void showScanError(@NonNull final ScanError error) {
        final Context context = getContext();
        if (context == null) {
            Logger.e(Logger.Topic.DISCOVERY, "Scan failed with " + error);
            return;
        }

        final String reasonStr;
        switch (error) {
            case ALREADY_STARTED:
                reasonStr = context.getString(R.string.scan_error_already_started);
                break;
            case INTERNAL_ERROR:
                reasonStr = context.getString(R.string.scan_error_internal);
                break;
            case PERMISSION_DENIED:
                reasonStr = context.getString(R.string.scan_error_permission_denied);
                break;
            case BLUETOOTH_DISABLED:
                reasonStr = context.getString(R.string.scan_error_bluetooth_disabled);
                break;
            case FEATURE_UNSUPPORTED:
                reasonStr = context.getString(R.string.scan_error_feature_unsupported);
                break;
            case APPLICATION_REGISTRATION_FAILED:
                reasonStr = context.getString(R.string.scan_error_application_registration_failed);
                break;
            case UNKNOWN:
            default:
                reasonStr = context.getString(R.string.scan_error_unknown);
                break;
        }

        Toast.makeText(context, context.getString(R.string.scan_failed, reasonStr),
            Toast.LENGTH_LONG)
            .show();
    }
}
