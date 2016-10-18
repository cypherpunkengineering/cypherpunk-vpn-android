package com.cypherpunk.android.vpn.ui.region;

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
import android.widget.TextView;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.model.Location;

import io.realm.Realm;


public class ConnectConfirmationDialogFragment extends DialogFragment {

    private static final String ARGS_LOCATION_ID = "location_id";

    public interface ConnectDialogListener {
        void onReconnectButtonClick();

        void onNoReconnectButtonClick();
    }

    public static ConnectConfirmationDialogFragment newInstance(@NonNull String locationId) {
        ConnectConfirmationDialogFragment f = new ConnectConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_LOCATION_ID, locationId);
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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_location_connect_now, container, false);
        TextView textView = (TextView) view.findViewById(R.id.city_name);
        String locationId = getArguments().getString(ARGS_LOCATION_ID);
        Realm realm = Realm.getDefaultInstance();
        Location location = realm.where(Location.class).equalTo("id", locationId).findFirst();
        textView.setText(location.getRegionName());
        realm.close();
        view.findViewById(R.id.rate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onReconnectButtonClick();
                }
                dismiss();
            }
        });
        view.findViewById(R.id.later_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onNoReconnectButtonClick();
                }
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
        show(fragmentManager, "ConnectConfirmationDialogFragment");
    }
}
