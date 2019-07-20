package com.bose.wearable.sample.viewmodels;

//
//  GestureEvent.java
//  BoseWearable
//
//  Created by Tambet Ingo on 02/08/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import com.bose.wearable.sensordata.GestureData;

import java.util.Date;

import androidx.annotation.NonNull;

public class GestureEvent {
    private final GestureData mGestureData;
    private final Date mDate;

    public GestureEvent(@NonNull final GestureData gestureData) {
        mGestureData = gestureData;
        mDate = new Date();
    }

    public GestureData gestureData() {
        return mGestureData;
    }

    public Date date() {
        return mDate;
    }
}
