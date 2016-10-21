package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cypherpunk.android.vpn.R;


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
        expirationViewView.setText(expiration);
    }

    public void setUsernameText(@NonNull String username) {
        this.username = username;
    }

    public void setRenewal(@NonNull String renewal) {
        this.renewal = renewal;
    }

    public void setExpiration(@NonNull String expiration) {
        this.expiration = expiration;
    }
}
