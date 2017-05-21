package com.cypherpunk.privacy.ui.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.cypherpunk.privacy.R;

public class AskReconnectDialogFragment extends DialogFragment {

    public interface ConnectDialogListener {
        void onConnectDialogButtonClick();
    }

    public static AskReconnectDialogFragment newInstance() {
        return new AskReconnectDialogFragment();
    }

    @Nullable
    private ConnectDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectDialogListener) {
            listener = (ConnectDialogListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.dialog_setting_connect_now, null);
        view.findViewById(R.id.connect_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onConnectDialogButtonClick();
                }
                dismiss();
            }
        });
        view.findViewById(R.id.later_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, "SettingConnectDialogFragment");
    }
}
