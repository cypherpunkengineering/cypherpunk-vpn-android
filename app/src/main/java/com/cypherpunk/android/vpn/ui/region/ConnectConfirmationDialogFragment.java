package com.cypherpunk.android.vpn.ui.region;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.cypherpunk.android.vpn.R;


public class ConnectConfirmationDialogFragment extends DialogFragment {

    public interface ConnectDialogListener {
        void onDialogPositiveButtonClick();

        void onDialogNegativeButtonClick();
    }

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
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.select_region_connect_dialog_message)
                .setPositiveButton(R.string.select_region_connect_dialog_positive,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (listener != null) {
                                    listener.onDialogPositiveButtonClick();
                                }
                            }
                        })
                .setNegativeButton(R.string.select_region_connect_negative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (listener != null) {
                                    listener.onDialogNegativeButtonClick();
                                }
                            }
                        })
                .create();
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, "ConnectConfirmationDialogFragment");
    }
}
