package com.bose.wearable.sample;

//
//  GestureDataAdapter.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/02/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.view.ViewGroup;

import com.bose.wearable.sample.viewholders.ValueViewHolder;
import com.bose.wearable.sample.viewmodels.GestureEvent;
import com.bose.wearable.sensordata.GestureData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GestureDataAdapter extends RecyclerView.Adapter<ValueViewHolder> {
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private final List<GestureEvent> mData = new ArrayList<>();

    @NonNull
    @Override
    public ValueViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                              final int viewType) {
        return ValueViewHolder.create(parent);
    }

    @SuppressWarnings("PMD.UnsynchronizedStaticDateFormatter") // This must be called from UI thread only
    @Override
    public void onBindViewHolder(@NonNull final ValueViewHolder holder,
                                 final int position) {
        final GestureEvent event = mData.get(position);
        final GestureData gestureData = event.gestureData();
        final String time = TIME_FORMAT.format(event.date());

        final String value = String.format(Locale.US, "%d (%s)", gestureData.timestamp(), time);
        holder.bind(gestureData.type().toString(), value);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addGestureEvent(@NonNull final GestureEvent gestureEvent) {
        mData.add(gestureEvent);
        notifyItemInserted(getItemCount() - 1);
    }

    public void replace(@NonNull final List<GestureEvent> events) {
        mData.clear();
        mData.addAll(events);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }
}
