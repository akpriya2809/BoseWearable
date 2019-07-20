package com.bose.wearable.sample.views;

//
//  SensorDataVector.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/17/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bose.wearable.sample.R;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.sensordata.Vector;
import com.bose.wearable.sensordata.VectorAccuracy;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.Locale;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

@SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // layout() is meant to be called from constructor
public class SensorDataVectorView extends ConstraintLayout {
    private Switch mSwitch;
    protected TextView mX;
    protected TextView mY;
    protected TextView mZ;
    protected TextView mTimestamp;
    protected TextView mAccuracy;
    protected TextView mFrequency;
    private EnabledListener mListener;
    protected String mFrequencyFormat;

    public interface EnabledListener {
        boolean onChanged(boolean isEnabled);
    }

    public SensorDataVectorView(final Context context) {
        super(context);
        init();
    }

    public SensorDataVectorView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SensorDataVectorView(final Context context,
                                final AttributeSet attrs,
                                final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final Context context = getContext();
        final View content = LayoutInflater.from(context)
            .inflate(layout(), this, true);

        mFrequencyFormat = context.getString(R.string.sensor_data_frequency_format);

        mSwitch = content.findViewById(R.id.enable_switch);
        mX = content.findViewById(R.id.x);
        mY = content.findViewById(R.id.y);
        mZ = content.findViewById(R.id.z);
        mTimestamp = content.findViewById(R.id.timestamp);
        mAccuracy = content.findViewById(R.id.accuracy);
        mFrequency = content.findViewById(R.id.frequency);

        numberValue(mX, 0.0);
        numberValue(mY, 0.0);
        numberValue(mZ, 0.0);
        numberValue(mTimestamp, 0);
        mAccuracy.setText(VectorAccuracy.UNRELIABLE.toString());
        frequency(0);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean checked) {
                final EnabledListener listener = mListener;
                if (listener != null) {
                    final boolean success = listener.onChanged(checked);
                    if (!success) {
                        mSwitch.setOnCheckedChangeListener(null);
                        mSwitch.toggle();
                        mSwitch.setOnCheckedChangeListener(this);
                    }
                }
            }
        });
    }

    @LayoutRes
    protected int layout() {
        return R.layout.sensor_data_vector;
    }

    public void enabledListener(@Nullable final EnabledListener listener) {
        mListener = listener;
    }

    public void sensorType(@NonNull final SensorType sensorType) {
        mSwitch.setText(sensorType.toString());
    }

    public void enabled(final boolean isEnabled) {
        mSwitch.setChecked(isEnabled);
    }

    public void sensorValue(@NonNull final SensorValue sensorValue) {
        numberValue(mTimestamp, sensorValue.timestamp());
        bindData(sensorValue);
    }

    public void frequency(final int frequency) {
        mFrequency.setText(String.format(Locale.US, mFrequencyFormat, frequency));
    }

    @SuppressWarnings("PMD.ReplaceVectorWithList") // PMD confuses SDK Vector with java.util.Vector
    protected void bindData(@NonNull final SensorValue sensorValue) {
        final Vector vector = sensorValue.vector();
        if (vector != null) {
            numberValue(mX, vector.x());
            numberValue(mY, vector.y());
            numberValue(mZ, vector.z());
        }

        final VectorAccuracy accuracy = sensorValue.vectorAccuracy();
        if (accuracy != null) {
            mAccuracy.setText(accuracy.toString());
        }
    }

    protected void numberValue(@NonNull final TextView textView, final double value) {
        textView.setText(String.format(Locale.US, "%.3f", value));
    }

    protected void numberValue(@NonNull final TextView textView, final int value) {
        textView.setText(String.valueOf(value));
    }
}
