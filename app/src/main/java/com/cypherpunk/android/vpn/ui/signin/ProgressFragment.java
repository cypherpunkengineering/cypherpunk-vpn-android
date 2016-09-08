package com.cypherpunk.android.vpn.ui.signin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.R;

/**
 * full screen progress dialog
 */
public class ProgressFragment extends DialogFragment {

    public static ProgressFragment newInstance() {
        return new ProgressFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_FullScreenProgressDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.dialog_progress, container, false);

    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, "ProgressFragment");
    }
}
