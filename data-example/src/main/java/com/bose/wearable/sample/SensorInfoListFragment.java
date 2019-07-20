package com.bose.wearable.sample;

//
//  SensorInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.sample.views.SubSectionView;
import com.bose.wearable.services.wearablesensor.SensorInformation;
import com.bose.wearable.services.wearablesensor.SensorType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

public class SensorInfoListFragment extends Fragment {
    private SessionViewModel mViewModel;

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
        return inflater.inflate(R.layout.fragment_sensor_info_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableSensorInfo()
            .observe(this, this::onSensorInfoRead);
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
        mViewModel.refreshWearableSensorInformation();
    }

    private void onSensorInfoRead(@NonNull final SensorInformation sensorInformation) {
        final ViewGroup list = (ViewGroup) getView();
        if (list == null) {
            return;
        }

        list.removeAllViews();

        for (final SensorType sensorType : sensorInformation.availableSensors()) {
            final SubSectionView sensorView = new SubSectionView(list.getContext());
            sensorView.setText(sensorType.toString());
            sensorView.setOnClickListener(v -> onSensorClicked(sensorType));
            list.addView(sensorView);
        }
    }

    private void onSensorClicked(@NonNull final SensorType sensorType) {
        final View view = getView();
        if (view != null) {
            final Bundle args = new Bundle();
            args.putSerializable(SensorInfoFragment.ARG_SENSOR_TYPE, sensorType);

            Navigation.findNavController(view)
                .navigate(R.id.action_sensorListFragment_to_sensorInfoFragment, args);
        }
    }
}
