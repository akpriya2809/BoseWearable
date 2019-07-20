package com.bose.wearable.sample;

//
//  GestureDataFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/02/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bose.wearable.sample.viewmodels.GestureEvent;
import com.bose.wearable.sample.viewmodels.SessionViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

public class GestureDataFragment extends Fragment {
    @SuppressWarnings("PMD.SingularField") // Need to keep a reference to it so it does not get GC'd
    private SessionViewModel mViewModel;
    private RecyclerView mRecyclerView;
    private GestureDataAdapter mAdapter;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gesture_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.list);
        mAdapter = new GestureDataAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.gestureEvents()
            .observe(this, event -> {
                final GestureEvent gestureEvent = event.get();
                if (gestureEvent != null) {
                    mAdapter.addGestureEvent(gestureEvent);
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            });

        mAdapter.replace(mViewModel.gestures());
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.gesture_data_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                mViewModel.clearGestures();
                mAdapter.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
