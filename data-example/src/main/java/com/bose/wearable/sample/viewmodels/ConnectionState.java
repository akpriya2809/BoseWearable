package com.bose.wearable.sample.viewmodels;

//
//  ConnectionState.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/16/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import androidx.annotation.NonNull;

import com.bose.blecore.Device;
import com.bose.blecore.Session;
import com.bose.wearable.wearabledevice.WearableDevice;

public abstract class ConnectionState {
    static final ConnectionState IDLE = new Idle();

    public static final class Idle extends ConnectionState {
        Idle() {
            // Hello PMD
        }
    }

    public static final class Connecting extends ConnectionState {
        @NonNull
        private final Destination mDestination;
        @NonNull
        private final Session mSession;

        public Connecting(@NonNull final Destination destination,
                          @NonNull final Session session) {
            mDestination = destination;
            mSession = session;
        }

        @NonNull
        public Destination destination() {
            return mDestination;
        }

        @NonNull
        public Session session() {
            return mSession;
        }
    }

    public static final class BondingRequired extends ConnectionState {
        @NonNull
        private final Destination mDestination;
        @NonNull
        private final Session mSession;

        public BondingRequired(@NonNull final Destination destination,
                               @NonNull final Session session) {
            mDestination = destination;
            mSession = session;
        }

        @NonNull
        public Destination destination() {
            return mDestination;
        }

        @NonNull
        public Session session() {
            return mSession;
        }
    }

    public static final class Connected extends ConnectionState {
        @NonNull
        private final Destination mDestination;
        @NonNull
        private final Session mSession;
        @NonNull
        private final WearableDevice mDevice;

        public Connected(@NonNull final Destination destination,
                         @NonNull final Session session) {
            mDestination = destination;
            mSession = session;

            final Device device = session.device();
            if (device instanceof WearableDevice) {
                mDevice = (WearableDevice) device;
            } else {
                throw new IllegalArgumentException("Session does not contain WearableDevice");
            }
        }

        @NonNull
        public Destination destination() {
            return mDestination;
        }

        @NonNull
        public Session session() {
            return mSession;
        }

        @NonNull
        public WearableDevice device() {
            return mDevice;
        }
    }
}