package com.bose.wearable.sample;

//
//  DevicePropertiesFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 03/29/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bose.wearable.sample.dialogs.AnrDialog;
import com.bose.wearable.sample.dialogs.CncDialog;
import com.bose.wearable.sample.dialogs.NameDialog;
import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.sample.views.TwoLineTextView;
import com.bose.wearable.services.bmap.AnrInformation;
import com.bose.wearable.services.bmap.AnrMode;
import com.bose.wearable.services.bmap.CncValue;
import com.bose.wearable.wearabledevice.DeviceProperties;

public class DevicePropertiesFragment extends Fragment {
    private static final int REQUEST_CODE_PRODUCT_NAME = 1;
    private static final int REQUEST_CODE_CNC = 2;
    private static final int REQUEST_CODE_ANR = 3;

    private SessionViewModel mViewModel;
    private TwoLineTextView mProtocolVersion;
    private TwoLineTextView mAuthSupported;
    private TwoLineTextView mGuid;
    private TwoLineTextView mProductName;
    private TwoLineTextView mBatteryLevel;
    private TwoLineTextView mCnc;
    private TwoLineTextView mAnr;
    @Nullable
    private DeviceProperties mProperties;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_properties_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProtocolVersion = view.findViewById(R.id.protocolVersionText);
        mAuthSupported = view.findViewById(R.id.authSupportedText);
        mGuid = view.findViewById(R.id.guidText);
        mProductName = view.findViewById(R.id.productNameText);
        mBatteryLevel = view.findViewById(R.id.batteryLevelText);
        mCnc = view.findViewById(R.id.cncText);
        mAnr = view.findViewById(R.id.anrText);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.monitorDeviceProperties()
            .observe(this, this::onDataLoaded);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.device_properties, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final CncValue cnc = mProperties != null ? mProperties.cnc() : null;
        final boolean cncSupported = cnc != null && cnc.steps() > 0;
        menu.findItem(R.id.update_cnc)
            .setEnabled(cncSupported);

        final AnrInformation anr = mProperties != null ? mProperties.anr() : null;
        final boolean anrSupported = anr != null && !anr.supported().isEmpty();
        menu.findItem(R.id.update_anr)
            .setEnabled(anrSupported);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                mViewModel.refreshDeviceProperties();
                return true;
            case R.id.update_name:
                updateName();
                return true;
            case R.id.update_cnc:
                updateCnc();
                return true;
            case R.id.update_anr:
                updateAnr();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PRODUCT_NAME:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    updateName(data.getStringExtra(NameDialog.EXTRA_NAME));
                }
                break;
            case REQUEST_CODE_CNC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    updateCnc(data.getIntExtra(CncDialog.EXTRA_LEVEL, -1),
                        data.getBooleanExtra(CncDialog.EXTRA_ENABLED, true));
                }
                break;
            case REQUEST_CODE_ANR:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    updateAnr((AnrMode) data.getSerializableExtra(AnrDialog.EXTRA_MODE));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void onDataLoaded(@NonNull final DeviceProperties deviceProperties) {
        mProperties = deviceProperties;

        mProtocolVersion.value(deviceProperties.protocolVersion());
        mAuthSupported.value(getString(deviceProperties.authenticationSupported() ? R.string.boolean_yes : R.string.boolean_no));
        mProductName.value(deviceProperties.productName());
        mBatteryLevel.value(deviceProperties.batteryLevel() + "%");

        final String guid = deviceProperties.guid();
        mGuid.value(guid != null ? guid : getString(R.string.not_available));

        final CncValue cnc = deviceProperties.cnc();
        if (cnc.enabled()) {
            mCnc.value(String.valueOf(cnc.steps() - (cnc.currentStep() + 1)));
        } else {
            mCnc.value(getString(R.string.cnc_disabled));
        }

        final AnrInformation anrInfo = deviceProperties.anr();
        if (anrInfo != null) {
            mAnr.value(anrInfo.current().toString());
        } else {
            mAnr.value(getString(R.string.not_available));
        }
    }

    private void updateName() {
        final NameDialog dialog = NameDialog.newInstance();
        dialog.setTargetFragment(this, REQUEST_CODE_PRODUCT_NAME);
        dialog.show(getFragmentManager(), "name-dialog");
    }

    private void updateName(@Nullable final String newName) {
        if (newName != null && !newName.isEmpty()) {
            mViewModel.changeProductName(newName);
        }
    }

    private void updateCnc() {
        final CncValue cnc = mProperties != null ? mProperties.cnc() : null;
        if (cnc == null) {
            return;
        }

        final CncDialog dialog = CncDialog.newInstance(cnc);
        dialog.setTargetFragment(this, REQUEST_CODE_CNC);
        dialog.show(getFragmentManager(), "cnc-dialog");
    }

    private void updateCnc(final int newLevel,
                           final boolean enabled) {
        final CncValue current = mProperties != null ? mProperties.cnc() : null;
        if (current != null && (current.currentStep() != newLevel || current.enabled() != enabled)) {
            mViewModel.changeCnc(newLevel, enabled);
        }
    }

    private void updateAnr() {
        final AnrInformation anrInfo = mProperties != null ? mProperties.anr() : null;
        if (anrInfo == null) {
            return;
        }

        final AnrDialog dialog = AnrDialog.newInstance(anrInfo);
        dialog.setTargetFragment(this, REQUEST_CODE_ANR);
        dialog.show(getFragmentManager(), "anr-dialog");
    }

    private void updateAnr(@Nullable final AnrMode mode) {
        if (mode == null) {
            return;
        }
        final AnrInformation anr = mProperties != null ? mProperties.anr() : null;
        if (anr == null) {
            return;
        }

        if (anr.current().equals(mode)) {
            return;
        }

        mViewModel.changeAnr(mode);
    }
}
