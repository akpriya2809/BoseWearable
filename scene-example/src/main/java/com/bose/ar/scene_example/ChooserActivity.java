package com.bose.ar.scene_example;

//
//  ChooserActivity.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/09/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.os.Bundle;

import com.bose.scene_example.R;
import com.bose.wearable.services.wearablesensor.SensorType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ChooserActivity extends AppCompatActivity implements ChooserFragment.Listener {
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new ChooserFragment())
                .commit();
        }
    }

    @Override
    public void onDeviceSelected(@NonNull final String deviceAddress,
                                 @NonNull final SensorType sensorType) {
        startActivity(MainActivity.intentForDevice(this, deviceAddress, sensorType));
    }

    @Override
    public void onSimulatedDeviceSelected(@NonNull final SensorType sensorType) {
        startActivity(MainActivity.intentForSimulatedDevice(this, sensorType));
    }
}
