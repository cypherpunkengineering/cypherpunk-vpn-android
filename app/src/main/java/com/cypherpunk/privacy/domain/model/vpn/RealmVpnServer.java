package com.cypherpunk.privacy.domain.model.vpn;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass
public class RealmVpnServer implements RealmModel, VpnServer {

    static final String KEY_ID = "id";
    static final String KEY_NAME = "name";
    static final String KEY_REGION = "region";
    static final String KEY_LEVEL = "level";
    static final String KEY_AUTHORIZED = "authorized";
    static final String KEY_OV_DEFAULT = "ovDefault";
    static final String KEY_LATENCY = "latency";
    static final String KEY_FAVORITE = "favorite";
    static final String KEY_DATE = "lastConnectedDate";

    @PrimaryKey
    @Required
    private String id;
    @Required
    private String name;
    @Required
    private String country;
    @Required
    private String region; // "NA", "SA", "AS" etc
    @Required
    private String level;
    private boolean authorized;
    @Required
    private String ovHostname;
    @Required
    private String ovDefault;
    @Required
    private String ovNone;
    @Required
    private String ovStrong;
    @Required
    private String ovStealth;

    private long latency = -1;
    private boolean favorite = false;
    @NonNull
    private Date lastConnectedDate = new Date(0);

    @SuppressWarnings("unused")
    public RealmVpnServer() {
    }

    RealmVpnServer(@NonNull String id,
                   @NonNull String name, @NonNull String country, @NonNull String region,
                   @NonNull String level, boolean authorized, @NonNull String ovHostname,
                   @NonNull String[] ovDefault, @NonNull String[] ovNone,
                   @NonNull String[] ovStrong, @NonNull String[] ovStealth) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.region = region;
        this.level = level;
        this.authorized = authorized;
        this.ovHostname = ovHostname;
        this.ovDefault = TextUtils.join(",", ovDefault);
        this.ovNone = TextUtils.join(",", ovNone);
        this.ovStrong = TextUtils.join(",", ovStrong);
        this.ovStealth = TextUtils.join(",", ovStealth);

        this.latency = -1;
        this.favorite = false;
        this.lastConnectedDate = new Date(0);
    }

    @NonNull
    @Override
    public String id() {
        return id;
    }

    @NonNull
    @Override
    public String name() {
        return name;
    }

    @NonNull
    @Override
    public String country() {
        return country;
    }

    @NonNull
    @Override
    public Level level() {
        if (TextUtils.isEmpty(ovDefault)) {
            return Level.UNAVAILABLE;
        }
        return Level.find(level);
    }

    @Override
    public boolean authorized() {
        return authorized;
    }

    @NonNull
    @Override
    public String ovHostname() {
        return ovHostname;
    }

    @NonNull
    @Override
    public String ovDefault() {
        return ovDefault;
    }

    @NonNull
    @Override
    public String ovNone() {
        return ovNone;
    }

    @NonNull
    @Override
    public String ovStrong() {
        return ovStrong;
    }

    @NonNull
    @Override
    public String ovStealth() {
        return ovStealth;
    }

    @Override
    public boolean isSelectable() {
        return authorized() && !TextUtils.isEmpty(ovDefault());
    }

    @Override
    public boolean favorite() {
        return favorite;
    }

    void update(@NonNull String name, @NonNull String country, @NonNull String region,
                @NonNull String level, boolean authorized, @NonNull String ovHostname,
                @NonNull String[] ovDefault, @NonNull String[] ovNone,
                @NonNull String[] ovStrong, @NonNull String[] ovStealth) {
        this.name = name;
        this.country = country;
        this.region = region;
        this.level = level;
        this.authorized = authorized;
        this.ovHostname = ovHostname;
        this.ovDefault = TextUtils.join(",", ovDefault);
        this.ovNone = TextUtils.join(",", ovNone);
        this.ovStrong = TextUtils.join(",", ovStrong);
        this.ovStealth = TextUtils.join(",", ovStealth);
    }

    void setLatency(long latency) {
        this.latency = latency;
    }

    void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    void setLastConnectedDate(@NonNull Date lastConnectedDate) {
        this.lastConnectedDate = lastConnectedDate;
    }

    @Override
    public String toString() {
        return "RealmVpnServer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", region='" + region + '\'' +
                ", level='" + level + '\'' +
                ", authorized=" + authorized +
                ", latency=" + latency +
                ", ovHostname='" + ovHostname + '\'' +
                ", ovDefault='" + ovDefault + '\'' +
                ", ovNone='" + ovNone + '\'' +
                ", ovStrong='" + ovStrong + '\'' +
                ", ovStealth='" + ovStealth + '\'' +
                ", favorite=" + favorite +
                ", lastConnectedDate=" + lastConnectedDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealmVpnServer that = (RealmVpnServer) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
