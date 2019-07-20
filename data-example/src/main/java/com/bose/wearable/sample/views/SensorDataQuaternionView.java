package com.bose.wearable.sample.views;

//
//  SensorDataQuaternionView.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/17/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.bose.wearable.sample.R;
import com.bose.wearable.sensordata.Quaternion;
import com.bose.wearable.sensordata.QuaternionAccuracy;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.Locale;

import androidx.annotation.NonNull;

public class SensorDataQuaternionView extends SensorDataVectorView {
    private static final Quaternion TRANSLATION_Q = new Quaternion(1, 0, 0, 0);

    private TextView mW;
    private TextView mPitch;
    private TextView mRoll;
    private TextView mYaw;

    public SensorDataQuaternionView(final Context context) {
        super(context);
        init();
    }

    public SensorDataQuaternionView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SensorDataQuaternionView(final Context context,
                                    final AttributeSet attrs,
                                    final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mW = findViewById(R.id.w);
        mPitch = findViewById(R.id.pitch);
        mRoll = findViewById(R.id.roll);
        mYaw = findViewById(R.id.yaw);

        numberValue(mW, 0.0);
        angleValue(mPitch, Math.PI);
        angleValue(mRoll, 0);
        angleValue(mYaw, 0);
    }

    @Override
    public void sensorType(@NonNull final SensorType sensorType) {
        super.sensorType(sensorType);

        if (sensorType == SensorType.GAME_ROTATION_VECTOR) {
            mAccuracy.setVisibility(View.GONE);
        } else if (sensorType == SensorType.ROTATION_VECTOR) {
            numberValue(mAccuracy, 0);
        }
    }

    @Override
    protected int layout() {
        return R.layout.sensor_data_quaternion;
    }

    @Override
    protected void bindData(@NonNull final SensorValue sensorValue) {
        final Quaternion quaternion = sensorValue.quaternion();

        if (quaternion != null) {
            numberValue(mX, quaternion.x());
            numberValue(mY, quaternion.y());
            numberValue(mZ, quaternion.z());
            numberValue(mW, quaternion.w());

            final Quaternion qResult = Quaternion.multiply(quaternion, TRANSLATION_Q);

            angleValue(mPitch, qResult.xRotation());
            angleValue(mRoll, -qResult.yRotation());
            angleValue(mYaw, -qResult.zRotation());
        }

        final QuaternionAccuracy accuracy = sensorValue.quaternionAccuracy();
        if (accuracy != null) {
            angleValue(mAccuracy, accuracy.estimatedAccuracy());
        }
    }

    private void angleValue(@NonNull final TextView textView,
                            final double radians) {
        final double degrees = radians * 180 / Math.PI;
        textView.setText(String.format(Locale.US, "%.2f°", degrees));
    }
}
