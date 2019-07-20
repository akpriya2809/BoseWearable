package com.bose.wearable.sample.views;

//
//  SubSectionView.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.bose.wearable.sample.R;
import com.google.android.material.button.MaterialButton;

public class SubSectionView extends MaterialButton {
    public SubSectionView(final Context context) {
        super(context);
        init();
    }

    public SubSectionView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SubSectionView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setAllCaps(false);
        setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
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
