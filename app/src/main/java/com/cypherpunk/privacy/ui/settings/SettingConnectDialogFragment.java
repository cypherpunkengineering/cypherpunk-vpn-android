package com.cypherpunk.privacy.ui.settings;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.cypherpunk.privacy.R;


public class SettingConnectDialogFragment extends DialogFragment {

    public interface ConnectDialogListener {
        void onConnectDialogButtonClick();
    }

    public static SettingConnectDialogFragment newInstance() {
        return new SettingConnectDialogFragment();
    }

    private ConnectDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectDialogListener) {
            listener = (ConnectDialogListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_setting_connect_now, container, false);
        view.findViewById(R.id.rate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onConnectDialogButtonClick();
                }
            }
        });
        view.findViewById(R.id.later_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, "SettingConnectDialogFragment");
    }
}