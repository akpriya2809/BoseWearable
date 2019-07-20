package com.bose.wearable.sample.views;

//
//  TwoLineTextView.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bose.wearable.sample.R;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public final class TwoLineTextView extends LinearLayout {
    private TextView mTitle;
    private TextView mValue;

    public TwoLineTextView(final Context context) {
        super(context);
        init(null);
    }

    public TwoLineTextView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TwoLineTextView(final Context context,
                           @Nullable final AttributeSet attrs,
                           final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public TwoLineTextView(final Context context,
                           final AttributeSet attrs,
                           final int defStyleAttr,
                           final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public void title(@Nullable final CharSequence text) {
        mTitle.setText(text);
    }

    public void title(@StringRes final int resId) {
        mTitle.setText(resId);
    }

    public void value(@Nullable final CharSequence text) {
        mValue.setText(text);
    }

    public void value(@StringRes final int resId) {
        mValue.setText(resId);
    }

    private void init(@Nullable final AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        final View content = LayoutInflater.from(getContext())
            .inflate(R.layout.two_line_text_view, this, true);

        mTitle = content.findViewById(R.id.title);
        mValue = content.findViewById(R.id.value);

        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.TwoLineTextView, 0, 0);

            title(a.getString(R.styleable.TwoLineTextView_title));
            a.recycle();
        }
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
