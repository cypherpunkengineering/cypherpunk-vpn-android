package com.cypherpunk.android.privacy.model;

import android.support.annotation.NonNull;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Network implements RealmModel {

    @PrimaryKey
    public String ssid;

    public boolean trusted;

    @SuppressWarnings("unused")
    public Network() {
    }

    public Network(@NonNull String ssid) {
        this.ssid = ssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }
}
