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

    private String username;
    private String renewal;
    private String expiration;

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
        TextView usernameView = (TextView) holder.itemView.findViewById(R.id.username);
        TextView renewalView = (TextView) holder.itemView.findViewById(R.id.renewal);
        TextView expirationViewView = (TextView) holder.itemView.findViewById(R.id.expiration);

        usernameView.setText(username);
        renewalView.setText(renewal);
        if (!TextUtils.isEmpty(expiration)) {
            expirationViewView.setText(getContext().getString(R.string.account_plan_expiration, expiration));
        }
    }

    public void setUsernameText(@NonNull String username) {
        this.username = username;
    }

    public void setRenewal(@NonNull String renewal) {
        switch (renewal) {
            case "none":
                this.renewal = getContext().getString(R.string.account_plan_none);
                break;
            case "monthly":
                this.renewal = getContext().getString(R.string.account_plan_monthly);
                break;
            case "semiannually":
                this.renewal = getContext().getString(R.string.account_plan_semiannually);
                break;
            case "annually":
                this.renewal = getContext().getString(R.string.account_plan_annually);
                break;
        }
    }

    public void setExpiration(@NonNull String expiration) {
        if (!TextUtils.isEmpty(expiration)) {
            return;
        }
        int timeStamp = Integer.parseInt(expiration);
        Date date = new Date((long) timeStamp * 1000);
        this.expiration = DateFormat.format(
                getContext().getString(R.string.account_plan_expiration_format), date).toString();
    }
}
