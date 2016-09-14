package com.cypherpunk.android.vpn.ui.region;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.model.Location;


public class ConnectConfirmationDialogFragment extends DialogFragment {

    private static final String ARGS_CITY = "city";

    public interface ConnectDialogListener {
        void onDialogPositiveButtonClick();

        void onDialogNegativeButtonClick();
    }

    // TODO: and national flag image url
    public static ConnectConfirmationDialogFragment newInstance(@NonNull Location location) {
        ConnectConfirmationDialogFragment f = new ConnectConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_CITY, location.getName());
        f.setArguments(args);
        return f;
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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_city, null, false);
        TextView textView = (TextView) view.findViewById(R.id.city_name);
        textView.setText(getArguments().getString(ARGS_CITY));
        return new AlertDialog.Builder(getActivity(), R.style.AppTheme_Alert)
                .setMessage(R.string.select_region_connect_dialog_message)
                .setView(view)
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
