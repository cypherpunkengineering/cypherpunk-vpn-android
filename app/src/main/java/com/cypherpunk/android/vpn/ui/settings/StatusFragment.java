package com.cypherpunk.android.vpn.ui.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.FragmentStatusBinding;


public class StatusFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentStatusBinding binding = FragmentStatusBinding.bind(getView());

        binding.map.setOriginalPosition(305, 56);
        binding.map.setNewPosition(20, 59);
    }
}
