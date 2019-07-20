package com.bose.ar.scene_example;

//
//  App.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/13/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.app.Application;

import com.bose.scene_example.BuildConfig;
import com.bose.wearable.BoseWearable;
import com.bose.wearable.Config;
import com.bose.wearable.focus.FocusMode;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final Config config = new Config.Builder()
            .focusMode(focusMode())
            .build();

        BoseWearable.configure(this, config);
    }

    private static FocusMode focusMode() {
        //noinspection ConstantConditions
        if ("MANUAL".equalsIgnoreCase(BuildConfig.FOCUS_MODE)) {
            return FocusMode.MANUAL;
        } else {
            return FocusMode.IGNORED;
        }
    }
}
