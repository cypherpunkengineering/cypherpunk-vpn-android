package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cypherpunk.android.vpn.R;

import java.util.Date;


public class AccountPreference extends Preference {

    private TextView usernameView;
    private TextView renewalView;
    private TextView expirationViewView;

    public AccountPreference(Context context) {
        this(context, null);
    }

    public AccountPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_account);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        usernameView = (TextView) holder.itemView.findViewById(R.id.username);
        renewalView = (TextView) holder.itemView.findViewById(R.id.renewal);
        expirationViewView = (TextView) holder.itemView.findViewById(R.id.expiration);
    }

    public void setUsernameText(@NonNull String username) {
        usernameView.setText(username);
    }

    public void setRenewal(@NonNull String renewal) {
        String renewalText = "";
        switch (renewal) {
            case "none":
                renewalText = getContext().getString(R.string.account_plan_none);
                break;
            case "monthly":
                renewalText = getContext().getString(R.string.account_plan_monthly);
                break;
            case "semiannually":
                renewalText = getContext().getString(R.string.account_plan_semiannually);
                break;
            case "annually":
                renewalText = getContext().getString(R.string.account_plan_annually);
                break;
        }
        renewalView.setText(renewalText);
    }

    public void setExpiration(@NonNull String expiration) {
        if (!TextUtils.isEmpty(expiration)) {
            return;
        }
        int timeStamp = Integer.parseInt(expiration);
        Date date = new Date((long) timeStamp * 1000);
        String expirationText = DateFormat.format(
                getContext().getString(R.string.account_plan_expiration_format), date).toString();
        if (!TextUtils.isEmpty(expiration)) {
            expirationViewView.setText(getContext().getString(R.string.account_plan_expiration, expirationText));
        }
    }
}
