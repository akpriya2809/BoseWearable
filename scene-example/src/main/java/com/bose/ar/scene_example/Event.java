package com.bose.ar.scene_example;

//
//  Event.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/29/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Event<T> {
    @NonNull
    private final T mContent;
    private boolean mHandled;

    public Event(@NonNull final T content) {
        mContent = content;
    }

    @NonNull
    public T peek() {
        return mContent;
    }

    @Nullable
    public T get() {
        if (mHandled) {
            return null;
        }

        mHandled = true;
        return mContent;
    }
}
