package com.bose.wearable.sample;

//
//  ObservedRateUpdater.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/02/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public class ObservedRateUpdater implements Handler.Callback {
    private final Handler mHandler;
    private final Listener mListener;
    private boolean mStarted;
    private int mUpdateCount;

    interface Listener {
        void onRateUpdated(final int rate);
    }

    public ObservedRateUpdater(@NonNull final Listener listener) {
         mHandler = new Handler(this);
         mListener = listener;
    }

    public void start() {
        if (mStarted) {
            return;
        }

        mStarted = true;
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    public void stop() {
        mHandler.removeMessages(0);
        mStarted = false;
    }

    public void updateReceived() {
        mUpdateCount += 1;
    }

    @Override
    public boolean handleMessage(final Message message) {
        mListener.onRateUpdated(mUpdateCount);
        mUpdateCount = 0;

        if (mStarted) {
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }

        return true;
    }
}
