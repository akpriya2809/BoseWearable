package com.bose.wearable.sample.views;

//
//  SensorDataVectorBiasView.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/19/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bose.wearable.sample.R;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.sensordata.Vector;

import androidx.annotation.NonNull;

public class SensorDataVectorBiasView extends SensorDataVectorView {
    private TextView mBiasX;
    private TextView mBiasY;
    private TextView mBiasZ;

    public SensorDataVectorBiasView(final Context context) {
        super(context);
        init();
    }

    public SensorDataVectorBiasView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SensorDataVectorBiasView(final Context context,
                                    final AttributeSet attrs,
                                    final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBiasX = findViewById(R.id.biasX);
        mBiasY = findViewById(R.id.biasY);
        mBiasZ = findViewById(R.id.biasZ);

        numberValue(mBiasX, 0.0);
        numberValue(mBiasY, 0.0);
        numberValue(mBiasZ, 0.0);
    }

    @Override
    protected int layout() {
        return R.layout.sensor_data_vector_bias;
    }

    @Override
    @SuppressWarnings("PMD.ReplaceVectorWithList") // PMD confuses SDK Vector with java.util.Vector
    protected void bindData(@NonNull final SensorValue sensorValue) {
        super.bindData(sensorValue);

        final Vector bias = sensorValue.bias();

        numberValue(mBiasX, bias.x());
        numberValue(mBiasY, bias.y());
        numberValue(mBiasZ, bias.z());
    }
}
