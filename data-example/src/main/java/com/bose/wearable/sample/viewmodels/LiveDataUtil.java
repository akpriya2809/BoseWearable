package com.bose.wearable.sample.viewmodels;

//
//  DistinctUntilChanged.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/16/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

final class LiveDataUtil {
    interface Predicate<T> {
        boolean matches(@Nullable T value);
    }

    private LiveDataUtil() {
    }

    static <T> LiveData<T> filter(@NonNull final LiveData<T> source,
                                  @NonNull final Predicate<T> predicate) {
        final MediatorLiveData<T> mediator = new MediatorLiveData<>();
        mediator.addSource(source, value -> {
            if (predicate.matches(value)) {
                mediator.setValue(value);
            }
        });

        return mediator;
    }

    static <T> LiveData<T> distinctUntilChanged(@NonNull final LiveData<T> source) {
        final MediatorLiveData<T> mediator = new MediatorLiveData<>();
        mediator.addSource(source, new Observer<T>() {
            private T mLastValue;

            @Override
            public void onChanged(final T value) {
                if (!Objects.equals(mLastValue, value)) {
                    mLastValue = value;
                    mediator.setValue(value);
                }
            }
        });

        return mediator;
    }
}
