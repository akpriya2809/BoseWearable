package com.bose.wearable.sample.viewholders;

//
//  ValueViewHolder.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/04/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bose.wearable.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ValueViewHolder extends RecyclerView.ViewHolder {
    private final TextView mLabel;
    private final TextView mValue;

    public ValueViewHolder(@NonNull final View view) {
        super(view);
        mLabel = view.findViewById(R.id.title);
        mValue = view.findViewById(R.id.value);
    }

    public void bind(@NonNull final String title,
                     @Nullable final String value) {
        mLabel.setText(title);
        mValue.setText(value);
    }

    public static ValueViewHolder create(@NonNull final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.value_item, parent, false);
        return new ValueViewHolder(view);
    }
}
