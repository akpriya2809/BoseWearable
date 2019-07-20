package com.bose.wearable.sample;

//
//  BaseWearableDeviceInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/31/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public abstract class BaseWearableDeviceInfoFragment extends Fragment {
    private SessionViewModel mViewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableDeviceInfo()
            .observe(this, this::onDataUpdated);
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

    protected abstract void onDataUpdated(@NonNull final WearableDeviceInformation deviceInformation);

    private void refreshData() {
        mViewModel.refreshWearableDeviceInformation();
    }
}
