package com.bose.ar.scene_example;

//
//  SensorValueReader.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import com.bose.wearable.sensordata.SensorValue;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("PMD.ReplaceVectorWithList") // Wrong Vector
public class ARCoreSensorValueReader {
    private final Quaternion mRoll180 = Quaternion.axisAngle(Vector3.right(), 180);
    private final float[] mBuffer = new float[4];
    private boolean mCorrectedInitially;
    @Nullable
    private Quaternion mInvertedInitialReading;
    private boolean mInverted;

    public boolean correctedInitially() {
        return mCorrectedInitially;
    }

    public void correctedInitially(final boolean value) {
        if (mCorrectedInitially != value) {
            mCorrectedInitially = value;
            resetInitialReading();
        }
    }

    public void resetInitialReading() {
        mInvertedInitialReading = null;
    }

    public boolean inverted() {
        return mInverted;
    }

    public void inverted(final boolean value) {
        mInverted = value;
    }

    @Nullable
    public Quaternion quaternion(@NonNull final SensorValue sensorValue) {
        if (!sensorValue.quaternion(mBuffer, 0)) {
            return null;
        }

        // Y and Z axis are swapped in ARCore
        // Flip across YZ plane (by negating X and W elements)
        Quaternion quaternion = new Quaternion(
            mBuffer[0] * -1,
            mBuffer[2],
            mBuffer[1],
            mBuffer[3] * -1);

        if (mCorrectedInitially) {
            if (mInvertedInitialReading == null) {
                mInvertedInitialReading = quaternion.inverted();
                return null;
            }

            quaternion = Quaternion.multiply(quaternion, mInvertedInitialReading);
        } else {
            quaternion = Quaternion.multiply(quaternion, mRoll180);
        }

        if (mInverted) {
            quaternion = quaternion.inverted();
        }

        return quaternion;
    }

    @Nullable
    public Vector3 vector(@NonNull final SensorValue sensorValue) {
        if (sensorValue.vector(mBuffer, 0)) {
            return new Vector3(mBuffer[0], mBuffer[1], mBuffer[2]);
        }

        return null;
    }

    @Nullable
    public Vector3 bias(@NonNull final SensorValue sensorValue) {
        if (sensorValue.bias(mBuffer, 0)) {
            return new Vector3(mBuffer[0], mBuffer[1], mBuffer[2]);
        }

        return null;
    }

    public static double yaw(@NonNull final Quaternion q) {
        final double x = q.x;
        final double y = q.y;
        final double z = q.z;
        final double w = q.w;
        final double siny = 2 * (w * y + x * z);
        final double cosy = 1 - 2 * (y * y + z * z);

        return -Math.atan2(siny, cosy);
    }

    public static double pitch(@NonNull final Quaternion q) {
        final double x = q.x;
        final double y = q.y;
        final double z = q.z;
        final double w = q.w;
        final double sinp = 2 * (w * x + y * z);
        final double cosp = 1 - 2 * (x * x + z * z);

        return Math.atan2(sinp, cosp);
    }

    public static double roll(@NonNull final Quaternion q) {
        final double x = q.x;
        final double y = q.y;
        final double z = q.z;
        final double w = q.w;
        final double sinr = 2 * (w * z - y * x);
        if (Math.abs(sinr) >= 1) {
            return -Math.copySign(Math.PI / 2, sinr);
        } else {
            return -Math.asin(sinr);
        }
    }
}
