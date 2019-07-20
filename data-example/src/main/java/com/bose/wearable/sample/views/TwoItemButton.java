package com.bose.wearable.sample.views;

//
//  TwoItemButton.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/24/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bose.wearable.sample.R;

import androidx.annotation.Nullable;

public class TwoItemButton extends LinearLayout {
    private TextView mTitle;
    private TextView mValue;

    public TwoItemButton(final Context context) {
        super(context);
        init();
    }

    public TwoItemButton(final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TwoItemButton(final Context context,
                         @Nullable final AttributeSet attrs,
                         final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TwoItemButton(final Context context,
                         final AttributeSet attrs,
                         final int defStyleAttr,
                         final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.ripple_drawable);

        final View content = LayoutInflater.from(getContext())
            .inflate(R.layout.two_item_button, this, true);

        mTitle = content.findViewById(R.id.title);
        mValue = content.findViewById(R.id.value);
    }

    public void title(@Nullable final String str) {
        mTitle.setText(str);
    }

    public void value(@Nullable final String val) {
        mValue.setText(val);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable parcelable = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(parcelable);
        savedState.enabled(isEnabled());
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setEnabled(savedState.enabled());
    }

    private static class SavedState extends BaseSavedState {
        private boolean mEnabled;

        SavedState(final Parcelable superState) {
            super(superState);
        }

        SavedState(final Parcel source) {
            super(source);
            mEnabled = source.readByte() == 1;
        }

        void enabled(final boolean isEnabled) {
            mEnabled = isEnabled;
        }

        boolean enabled() {
            return mEnabled;
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mEnabled ? 1 : 0));
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(final Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(final int size) {
                return new SavedState[size];
            }
        };
    }
}
