package com.cypherpunk.android.vpn.ui.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.FragmentSettingsBinding;


public class SettingsFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding = FragmentSettingsBinding.bind(getView());

        binding.radioGroup.setOnCheckedChangeListener(this);
        binding.maxSpeedRadioButton.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        // TODO:
    }
}
