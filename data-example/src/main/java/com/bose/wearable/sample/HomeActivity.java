package com.bose.wearable.sample;

//
//  HomeActivity.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/15/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity implements HomeFragment.Listener {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new HomeFragment())
                .commit();
        }
    }

    @Override
    public void onDeviceSelected(@NonNull final String deviceAddress) {
        startActivity(MainActivity.intentForDevice(this, deviceAddress));
    }

    @Override
    public void onSimulatedDeviceSelected() {
        startActivity(MainActivity.intentForSimulatedDevice(this));
    }
}
