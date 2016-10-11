package com.cypherpunk.android.vpn.model;

import android.support.annotation.NonNull;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Network implements RealmModel {

    @PrimaryKey
    private String ssid;

    private boolean check;

    @SuppressWarnings("unused")
    public Network() {
    }

    public Network(@NonNull String ssid, boolean check) {
        this.ssid = ssid;
        this.check = check;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

}
