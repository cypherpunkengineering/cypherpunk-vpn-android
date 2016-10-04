package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.cypherpunk.android.vpn.R;


public class EncryptionPreference extends Preference {

    private String cipher;
    private String auth;
    private String key;

    public EncryptionPreference(Context context) {
        this(context, null);
    }

    public EncryptionPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EncryptionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_encryption);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View container = holder.itemView.findViewById(R.id.detail_container);

        TextView cipherView = (TextView) container.findViewById(R.id.cipher);
        cipherView.setText(cipher);

        TextView authView = (TextView) container.findViewById(R.id.auth);
        authView.setText(auth);

        TextView keyView = (TextView) container.findViewById(R.id.key);
        keyView.setText(key);
    }

    public void setAuthText(@NonNull String auth) {
        this.auth = auth;
    }

    public void setCipherText(@NonNull String cipher) {
        this.cipher = cipher;
    }

    public void setKeyText(@NonNull String key) {
        this.key = key;
    }
}
