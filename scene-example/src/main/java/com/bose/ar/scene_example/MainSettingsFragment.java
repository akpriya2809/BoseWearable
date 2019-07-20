package com.bose.ar.scene_example;

//
//  MainSettingsFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 11/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.SharedPreferences;
import android.os.Bundle;

import com.bose.scene_example.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class MainSettingsFragment extends PreferenceFragmentCompat {
    public static final String PREF_CORRECTED_INITIALLY = "corrected_initially";
    public static final String PREF_MIRROR = "mirror";
    private static final String PREF_RESET_INITIAL = "reset_initial";

    private SharedPreferences mPrefs;
    private SensorViewModel mViewModel;
    private Preference mResetPref;
    private final SharedPreferences.OnSharedPreferenceChangeListener mListener = (sharedPreferences, key) -> {
            if (PREF_CORRECTED_INITIALLY.equals(key)) {
                mResetPref.setEnabled(mPrefs.getBoolean(PREF_CORRECTED_INITIALLY, false));
            }
    };

    @Override
    public void onCreatePreferences(@Nullable final Bundle savedInstanceState,
                                    @Nullable final String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
        mResetPref = getPreferenceScreen().findPreference(PREF_RESET_INITIAL);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPrefs = getPreferenceManager().getSharedPreferences();
        mViewModel = ViewModelProviders.of(requireActivity()).get(SensorViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefs.registerOnSharedPreferenceChangeListener(mListener);
        mListener.onSharedPreferenceChanged(mPrefs, PREF_CORRECTED_INITIALLY);
    }

    @Override
    public void onPause() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(mListener);
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull final Preference preference) {
        final String key = preference.getKey();
        if (PREF_CORRECTED_INITIALLY.equals(key)) {
            mViewModel.correctedInitially(mPrefs.getBoolean(key, false));
        } else if (PREF_RESET_INITIAL.equals(key)) {
            mViewModel.resetInitialReading();
        } else if (PREF_MIRROR.equals(key)) {
            mViewModel.inverted(mPrefs.getBoolean(key, true));
        } else {
            return super.onPreferenceTreeClick(preference);
        }

        return true;
    }
}
