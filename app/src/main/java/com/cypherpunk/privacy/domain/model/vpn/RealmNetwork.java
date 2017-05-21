package com.cypherpunk.privacy.domain.model.vpn;

import android.support.annotation.NonNull;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class RealmNetwork implements RealmModel, Network {

    @PrimaryKey
    public String ssid;

    public boolean trusted;

    @SuppressWarnings("unused")
    public RealmNetwork() {
    }

    public RealmNetwork(@NonNull String ssid) {
        this.ssid = ssid;
    }

    @Override
    public String ssid() {
        return ssid;
    }

    @Override
    public boolean trusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }
}
