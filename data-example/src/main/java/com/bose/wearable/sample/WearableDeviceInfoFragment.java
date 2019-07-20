package com.bose.wearable.sample;

//
//  ARDeviceInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/03/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.bose.wearable.sample.views.TwoLineTextView;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;

public class WearableDeviceInfoFragment extends BaseWearableDeviceInfoFragment {
    private TwoLineTextView mMajorVersion;
    private TwoLineTextView mMinorVersion;
    private TwoLineTextView mProductId;
    private TwoLineTextView mVariant;
    private TwoLineTextView mTransmissionPeriod;
    private TwoLineTextView mMaxPayload;
    private TwoLineTextView mMaxActiveSensors;
    private TwoLineTextView mDeviceStatus;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wearable_device_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMajorVersion = view.findViewById(R.id.majorVersionText);
        mMinorVersion = view.findViewById(R.id.minorVersionText);
        mProductId = view.findViewById(R.id.productIdText);
        mVariant = view.findViewById(R.id.variantText);
        mTransmissionPeriod = view.findViewById(R.id.transmissionPeriodText);
        mMaxPayload = view.findViewById(R.id.maxPayloadText);
        mMaxActiveSensors = view.findViewById(R.id.maxActiveSensorsText);
        mDeviceStatus = view.findViewById(R.id.deviceStatusText);

        final View availableSensors = view.findViewById(R.id.availableSensorsText);
        availableSensors.setOnClickListener(v -> showAvailableSensors());

        final View availableGestures = view.findViewById(R.id.availableGesturesText);
        availableGestures.setOnClickListener(v -> showAvailableGestures());
    }

    protected void onDataUpdated(@NonNull final WearableDeviceInformation info) {
        mMajorVersion.value(String.valueOf(info.majorVersion()));
        mMinorVersion.value(String.valueOf(info.minorVersion()));
        mProductId.value(info.productInfo().idName());
        mVariant.value(info.productInfo().variantName());
        mTransmissionPeriod.value(String.valueOf(info.transmissionPeriod()));
        mMaxPayload.value(String.valueOf(info.maxPayload()));
        mMaxActiveSensors.value(String.valueOf(info.maxActiveSensors()));
        mDeviceStatus.value(info.deviceStatus().toString());
    }

    private void showAvailableSensors() {
        final View view = getView();
        if (view != null) {
            Navigation.findNavController(view)
                .navigate(R.id.action_ARDeviceInfoFragment_to_availableSensorsFragment);
        }
    }

    private void showAvailableGestures() {
        final View view = getView();
        if (view != null) {
            Navigation.findNavController(view)
                .navigate(R.id.action_ARDeviceInfoFragment_to_availableGesturesFragment);
        }
    }
}
