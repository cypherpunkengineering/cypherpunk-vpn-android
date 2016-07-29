package com.cypherpunk.android.vpn.ui.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.FragmentSimpleSettingsBinding;


public class SimpleSettingsFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private FragmentSimpleSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding = FragmentSimpleSettingsBinding.bind(getView());

        binding.radioGroup.setOnCheckedChangeListener(this);
        binding.maxSpeedRadioButton.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        int selectedModeName = R.string.settings_mode_max_speed;
        switch (id) {
            case R.id.max_privacy_radio_button:
                selectedModeName = R.string.settings_mode_max_privacy;
                break;
            case R.id.max_speed_radio_button:
                selectedModeName = R.string.settings_mode_max_speed;
                break;
            case R.id.max_freedom_radio_button:
                selectedModeName = R.string.settings_mode_max_freedom;
                break;
        }
        binding.selectedModeTitle.setText(selectedModeName);
    }
}
