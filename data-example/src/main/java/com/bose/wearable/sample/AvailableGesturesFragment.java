package com.bose.wearable.sample;

//
//  AvailableGesturesFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/04/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.bose.wearable.services.wearablesensor.GestureType;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AvailableGesturesFragment extends BaseWearableDeviceInfoFragment {
    private CheckedTextView mSingleTap;
    private CheckedTextView mDoubleTap;
    private CheckedTextView mHeadNod;
    private CheckedTextView mHeadShake;
    private CheckedTextView mTouchAndHold;
    private CheckedTextView mInput;
    private CheckedTextView mAffirmative;
    private CheckedTextView mNegative;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_available_gestures, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSingleTap = view.findViewById(R.id.singleTapText);
        mDoubleTap = view.findViewById(R.id.doubleTapText);
        mHeadNod = view.findViewById(R.id.headNodText);
        mHeadShake = view.findViewById(R.id.headShakeText);
        mTouchAndHold = view.findViewById(R.id.touchAndHoldText);
        mInput = view.findViewById(R.id.inputText);
        mAffirmative = view.findViewById(R.id.affirmativeText);
        mNegative = view.findViewById(R.id.negativeText);
    }

    protected void onDataUpdated(@NonNull final WearableDeviceInformation deviceInformation) {
        final Set<GestureType> available = deviceInformation.availableGestures();

        mSingleTap.setChecked(available.contains(GestureType.SINGLE_TAP));
        mDoubleTap.setChecked(available.contains(GestureType.DOUBLE_TAP));
        mHeadNod.setChecked(available.contains(GestureType.HEAD_NOD));
        mHeadShake.setChecked(available.contains(GestureType.HEAD_SHAKE));
        mTouchAndHold.setChecked(available.contains(GestureType.TOUCH_AND_HOLD));
        mInput.setChecked(available.contains(GestureType.INPUT));
        mAffirmative.setChecked(available.contains(GestureType.AFFIRMATIVE));
        mNegative.setChecked(available.contains(GestureType.NEGATIVE));
    }
}
