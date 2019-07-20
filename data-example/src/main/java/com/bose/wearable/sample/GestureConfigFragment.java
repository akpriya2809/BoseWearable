package com.bose.wearable.sample;

//
//  GestureConfigFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/25/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.services.wearablesensor.GestureConfiguration;
import com.bose.wearable.services.wearablesensor.GestureType;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class GestureConfigFragment extends Fragment {
    private SessionViewModel mViewModel;
    private ViewGroup mGesturesList;
    @NonNull
    private GestureConfiguration mGestureConf = GestureConfiguration.EMPTY;

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
        return inflater.inflate(R.layout.fragment_gesture_conf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        mGesturesList = view.findViewById(R.id.gestures_list);

        final Button enableAll = view.findViewById(R.id.enable_all_button);
        enableAll.setOnClickListener(v -> enableAll());

        final Button disableAll = view.findViewById(R.id.disable_all_button);
        disableAll.setOnClickListener(v -> disableAll());
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableGestureConfiguration()
            .observe(this, this::onGesturesRead);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                refreshData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshData() {
        mViewModel.refreshGestureConfiguration();
    }

    @SuppressWarnings("deprecation")
    private void onGesturesRead(@NonNull final GestureConfiguration gestureConfiguration) {
        mGestureConf = gestureConfiguration;
        mGesturesList.removeAllViews();

        final List<GestureType> list = gestureConfiguration.allGestures();
        Collections.sort(list, (a, b) -> a.value() > b.value() ? 1 : 0);
        final Context context = mGesturesList.getContext();

        final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
            context.getResources().getDisplayMetrics());

        for (final GestureType gesture : list) {
            final CheckBox checkBox = new CheckBox(context);
            checkBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkBox.setTextAppearance(android.R.style.TextAppearance_Material_Subhead);
            } else {
                checkBox.setTextAppearance(context, android.R.style.TextAppearance_Material_Subhead);
            }

            checkBox.setButtonDrawable(R.drawable.checked_text_mark);
            checkBox.setText(gesture.toString());
            checkBox.setChecked(gestureConfiguration.gestureEnabled(gesture));
            checkBox.setOnCheckedChangeListener((compoundButton, checked) -> set(gesture, checked));

            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = margin;

            mGesturesList.addView(checkBox, params);
        }
    }

    private void set(@NonNull final GestureType gestureType, final boolean enabled) {
        doChange(mGestureConf.gestureEnabled(gestureType, enabled));
    }

    private void enableAll() {
        doChange(mGestureConf.enableAll());
    }

    private void disableAll() {
        doChange(mGestureConf.disableAll());
    }

    private void doChange(@NonNull final GestureConfiguration newConf) {
        if (!newConf.equals(mGestureConf)) {
            mViewModel.changeGestureConfiguration(newConf);
        }
    }
}

