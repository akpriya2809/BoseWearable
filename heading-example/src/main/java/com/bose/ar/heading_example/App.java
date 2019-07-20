package com.bose.ar.heading_example;

//
//  App.java
//  BoseWearable
//
//  Created by Tambet Ingo on 02/19/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.app.Application;

import com.bose.wearable.BoseWearable;
import com.bose.wearable.Config;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BoseWearable.configure(this, new Config.Builder().build());
    }
}
