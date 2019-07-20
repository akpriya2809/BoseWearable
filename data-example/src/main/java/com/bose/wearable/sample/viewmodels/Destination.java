package com.bose.wearable.sample.viewmodels;

//
//  Destination.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/16/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import com.bose.blecore.DiscoveredDevice;
import com.bose.blecore.Session;
import com.bose.wearable.BoseWearable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Destination {
    @Nullable
    private final DiscoveredDevice mDiscoveredDevice;

    private Destination(@Nullable final DiscoveredDevice device) {
        mDiscoveredDevice = device;
    }

    @NonNull
    public static Destination device(@NonNull final DiscoveredDevice device) {
        return new Destination(device);
    }

    @Nullable
    public static Destination device(@NonNull final String deviceAddress) {
        final DiscoveredDevice device = BoseWearable.getInstance()
            .bluetoothManager()
            .deviceByAddress(deviceAddress);

        if (device != null) {
            return new Destination(device);
        } else {
            return null;
        }
    }

    @NonNull
    public static Destination simulated() {
        return new Destination(null);
    }

    @NonNull
    public Session createSession() {
        final BoseWearable boseWearable = BoseWearable.getInstance();
        if (mDiscoveredDevice != null) {
            return boseWearable.bluetoothManager()
                .session(mDiscoveredDevice);
        } else {
            return boseWearable.createSimulatedSession();
        }
    }

    public String name() {
        if (mDiscoveredDevice != null) {
            return mDiscoveredDevice.localName();
        } else {
            return "Simulated Device";
        }
    }

    public boolean isSimulated() {
        return mDiscoveredDevice == null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Destination that = (Destination) o;
        return Objects.equals(mDiscoveredDevice, that.mDiscoveredDevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDiscoveredDevice);
    }
}
