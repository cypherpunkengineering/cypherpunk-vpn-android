package com.cypherpunk.privacy.model;

import android.support.annotation.NonNull;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass
public class Region implements RealmModel {

    @PrimaryKey
    @Required
    private String id;

    @Required
    private String regionName;

    private boolean regionEnabled;

    @Required
    private String region;

    @Required
    private String country;

    @Required
    private String ovHostname;

    @Required
    private RealmList<RealmString> ovDefault;
    @Required
    private RealmList<RealmString> ovNone;
    @Required
    private RealmList<RealmString> ovStrong;
    @Required
    private RealmList<RealmString> ovStealth;

    private boolean favorited;

    private Date lastConnectedDate;

    @SuppressWarnings("unused")
    public Region() {
    }

    public Region
    (
         @NonNull String id,
         @NonNull String region,
         @NonNull String country,
         @NonNull String regionName,
         boolean regionEnabled,

         @NonNull String ovHostname,

         @NonNull String[] ovDefault,
         @NonNull String[] ovNone,
         @NonNull String[] ovStrong,
         @NonNull String[] ovStealth
    )
    {
        this.id = id;
        this.region = region;
        this.country = country;
        this.regionName = regionName;
        this.regionEnabled = regionEnabled;

        this.ovHostname = ovHostname;

        this.ovDefault = new RealmList<>();
        for (String s : ovDefault) {
            this.ovDefault.add(new RealmString(s));
        }
        this.ovNone = new RealmList<>();
        for (String s : ovNone) {
            this.ovNone.add(new RealmString(s));
        }
        this.ovStrong = new RealmList<>();
        for (String s : ovStrong) {
            this.ovStrong.add(new RealmString(s));
        }
        this.ovStealth = new RealmList<>();
        for (String s : ovStealth) {
            this.ovStealth.add(new RealmString(s));
        }
        this.lastConnectedDate = new Date(0);
    }

    public String getId() { return id; }

    public String getRegion() {
        return region;
    }

    public String getCountry() { return country; }
    public String getRegionName() { return regionName; }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public boolean isRegionEnabled() {
        return regionEnabled;
    }

    public String getOvHostname() { return ovHostname; }
    public void setOvHostname(String ovHostname) { this.ovHostname = ovHostname; }

    public RealmList<RealmString> getOvDefault() {
        return ovDefault;
    }

    public void setOvDefault(RealmList<RealmString> ovDefault) {
        this.ovDefault = ovDefault;
    }

    public void setOvDefault(String[] ovDefault) {
        this.ovDefault = new RealmList<>();
        for (String s : ovDefault) {
            this.ovDefault.add(new RealmString(s));
        }
    }

    public RealmList<RealmString> getOvNone() {
        return ovNone;
    }

    public void setOvNone(RealmList<RealmString> ovNone) {
        this.ovNone = ovNone;
    }

    public void setOvNone(String[] ovNone) {
        this.ovNone = new RealmList<>();
        for (String s : ovNone) {
            this.ovNone.add(new RealmString(s));
        }
    }

    public RealmList<RealmString> getOvStrong() {
        return ovStrong;
    }

    public void setOvStrong(RealmList<RealmString> ovStrong) {
        this.ovStrong = ovStrong;
    }

    public void setOvStrong(String[] ovStrong) {
        this.ovStrong = new RealmList<>();
        for (String s : ovStrong) {
            this.ovStrong.add(new RealmString(s));
        }
    }

    public RealmList<RealmString> getOvStealth() {
        return ovStealth;
    }

    public void setOvStealth(RealmList<RealmString> ovStealth) {
        this.ovStealth = ovStealth;
    }

    public void setOvStealth(String[] ovStealth) {
        this.ovStealth = new RealmList<>();
        for (String s : ovStealth) {
            this.ovStealth.add(new RealmString(s));
        }    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public Date getLastConnectedDate() {
        return lastConnectedDate;
    }

    public void setLastConnectedDate(Date lastConnectedDate) {
        this.lastConnectedDate = lastConnectedDate;
    }

    @Override
    public String toString() {
        return "Region{" +
                "id='" + id + '\'' +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                ", regionName='" + regionName + '\'' +
                ", regionEnabled='" + regionEnabled + '\'' +
                ", ovHostname='" + ovHostname + '\'' +
                ", ovDefault='" + ovDefault + '\'' +
                ", ovNone='" + ovNone + '\'' +
                ", ovStrong='" + ovStrong + '\'' +
                ", ovStealth='" + ovStealth + '\'' +
                ", favorited=" + favorited +
                ", favorited=" + lastConnectedDate.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;

        final Region region = (Region) o;
        return id != null ? id.equals(region.id) : region.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
