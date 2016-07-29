package com.cypherpunk.android.vpn.ui.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.FragmentSimpleSettingsBinding;


public class SimpleSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentSimpleSettingsBinding binding = FragmentSimpleSettingsBinding.bind(getView());

        binding.maxSpeedRadioButton.setChecked(true);
    }
}
