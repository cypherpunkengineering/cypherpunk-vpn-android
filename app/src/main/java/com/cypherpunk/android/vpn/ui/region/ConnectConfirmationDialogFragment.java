package com.cypherpunk.android.vpn.ui.region;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_connect_now, container, false);
        TextView textView = (TextView) view.findViewById(R.id.city_name);
        textView.setText(getArguments().getString(ARGS_CITY));
        view.findViewById(R.id.rate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDialogPositiveButtonClick();
                }
            }
        });
        view.findViewById(R.id.later_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDialogNegativeButtonClick();
                }
            }
        });
        return view;
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, "ConnectConfirmationDialogFragment");
    }
}
