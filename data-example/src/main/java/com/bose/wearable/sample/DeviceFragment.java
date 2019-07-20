package com.bose.wearable.sample;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bose.wearable.sample.viewmodels.SessionViewModel;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

public class DeviceFragment extends Fragment {
    private SessionViewModel mViewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.device_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View deviceInfo = view.findViewById(R.id.device_info_button);
        deviceInfo.setOnClickListener(v -> onDeviceInfoSelected());

        final View deviceProperties = view.findViewById(R.id.device_properties_button);
        deviceProperties.setOnClickListener(v -> onDevicePropertiesSelected());

        final View wearableDeviceInfo = view.findViewById(R.id.wearable_device_info_button);
        wearableDeviceInfo.setOnClickListener(v -> onWearableDeviceInfoSelected());

        final View wearableSensorInfo = view.findViewById(R.id.sensor_info_button);
        wearableSensorInfo.setOnClickListener(v -> onWearableSensorInfoSelected());

        final View wearableSensorConf = view.findViewById(R.id.sensor_conf_button);
        wearableSensorConf.setOnClickListener(v -> onWearableSensorConfSelected());

        final View wearableSensorData = view.findViewById(R.id.sensor_data_button);
        wearableSensorData.setOnClickListener(v -> onWearableSensorDataSelected());

        final View wearableGestureConf = view.findViewById(R.id.gestures_configuration);
        wearableGestureConf.setOnClickListener(v -> onWearableGestureConfSelected());

        final View wearableGestureData = view.findViewById(R.id.gestures_data);
        wearableGestureData.setOnClickListener(v -> onWearableGestureDataSelected());
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.device_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.device_services_refresh:
                mViewModel.refreshServices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDeviceInfoSelected() {
        navigateTo(R.id.action_deviceFragment_to_deviceInfoFragment);
    }

    private void onDevicePropertiesSelected() {
        navigateTo(R.id.action_deviceFragment_to_devicePropertiesFragment);
    }

    private void onWearableDeviceInfoSelected() {
        navigateTo(R.id.action_deviceFragment_to_WearableDeviceInfoFragment);
    }

    private void onWearableSensorInfoSelected() {
        navigateTo(R.id.action_deviceFragment_to_sensorInfoListFragment);
    }

    private void onWearableSensorConfSelected() {
        navigateTo(R.id.action_deviceFragment_to_sensorConfigFragment);
    }

    private void onWearableSensorDataSelected() {
        navigateTo(R.id.action_deviceFragment_to_sensorDataFragment);
    }

    private void onWearableGestureConfSelected() {
        navigateTo(R.id.action_deviceFragment_to_gestureConfigFragment);
    }

    private void onWearableGestureDataSelected() {
        navigateTo(R.id.action_deviceFragment_to_gestureDataFragment);
    }

    private void navigateTo(@IdRes final int destination) {
        final View view = getView();
        if (view != null) {
            Navigation.findNavController(view)
                .navigate(destination);
        }
    }
}
