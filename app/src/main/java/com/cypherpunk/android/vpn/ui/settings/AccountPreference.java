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
        TextView textView = (TextView) holder.itemView.findViewById(R.id.username);
        textView.setText(username);
    }

    public void setUsernameText(@NonNull String username) {
        this.username = username;
    }
}
