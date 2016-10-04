package com.cypherpunk.android.vpn.ui.settings;

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

import com.cypherpunk.android.vpn.R;


public class RateDialogFragment extends DialogFragment {

    public interface RateDialogListener {
        void onRateNowButtonClick();
    }

    public static RateDialogFragment newInstance() {
        return new RateDialogFragment();
    }

    private RateDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RateDialogListener) {
            listener = (RateDialogListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_rate, container, false);
        view.findViewById(R.id.rate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onRateNowButtonClick();
                    dismiss();
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
        show(fragmentManager, "RateDialogFragment");
    }
}
