package com.bose.wearable.sample;

//
//  DeviceInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/31/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bose.blecore.deviceinformation.DeviceInformation;
import com.bose.blecore.util.Util;
import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.sample.views.TwoLineTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class DeviceInfoFragment extends Fragment {
    private SessionViewModel mViewModel;
    private TwoLineTextView mSystemId;
    private TwoLineTextView mModelNumber;
    private TwoLineTextView mSerialNumber;
    private TwoLineTextView mFirmwareRev;
    private TwoLineTextView mHardwareRev;
    private TwoLineTextView mSoftwareRev;
    private TwoLineTextView mManufacturerName;
    private TwoLineTextView mRegulatoryCert;
    private TwoLineTextView mPnpId;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_info_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSystemId = view.findViewById(R.id.systemIdText);
        mModelNumber = view.findViewById(R.id.modelNumberText);
        mSerialNumber = view.findViewById(R.id.serialNumberText);
        mFirmwareRev = view.findViewById(R.id.firmwareRevText);
        mHardwareRev = view.findViewById(R.id.hardwareRevText);
        mSoftwareRev = view.findViewById(R.id.softwareRevText);
        mManufacturerName = view.findViewById(R.id.manufacturerNameText);
        mRegulatoryCert = view.findViewById(R.id.regulatoryCertText);
        mPnpId = view.findViewById(R.id.pnpIdText);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.monitorDeviceInfo()
            .observe(this, this::onDataLoaded);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                mViewModel.refreshDeviceInformation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDataLoaded(@NonNull final DeviceInformation info) {
        mSystemId.value(Util.bytesToHexString(info.systemId()));
        mModelNumber.value(info.modelNumber());
        mSerialNumber.value(info.serialNumber());
        mFirmwareRev.value(info.firmwareRevision());
        mHardwareRev.value(info.hardwareRevision());
        mSoftwareRev.value(info.softwareRevision());
        mManufacturerName.value(info.manufacturerName());
        mRegulatoryCert.value(Util.bytesToHexString(info.regulatoryCertifications()));
        mPnpId.value(Util.bytesToHexString(info.pnpId()));
    }
}
