package com.cypherpunk.privacy.model;

import android.support.annotation.NonNull;

import java.util.Date;

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
    @Required
    private String countryCode;

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

    private boolean favorited;

    private Date lastConnectedDate;

    @SuppressWarnings("unused")
    public Region() {
    }

    public Region
    (
         @NonNull String id,
         @NonNull String countryCode,
         @NonNull String regionName,

         @NonNull String ovHostname,

         @NonNull String ovDefault,
         @NonNull String ovNone,
         @NonNull String ovStrong,
         @NonNull String ovStealth
    )
    {
        this.id = id;
        this.countryCode = countryCode;
        this.regionName = regionName;

        this.ovHostname = ovHostname;

        this.ovDefault = ovDefault;
        this.ovNone = ovNone;
        this.ovStrong = ovStrong;
        this.ovStealth = ovStealth;
        this.lastConnectedDate = new Date(0);
    }

    public String getId() { return id; }

    public String getCountryCode() { return countryCode; }
    public String getRegionName() { return regionName; }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getOvHostname() { return ovHostname; }
    public void setOvHostname(String ovHostname) { this.ovHostname = ovHostname; }

    public String getOvDefault() { return ovDefault; }
    public void setOvDefault(String ovDefault) { this.ovDefault = ovDefault; }
    public String getOvNone() { return ovNone; }
    public void setOvNone(String ovNone) { this.ovNone = ovNone; }
    public String getOvStrong() { return ovStrong; }
    public void setOvStrong(String ovStrong) { this.ovStrong = ovStrong; }
    public String getOvStealth() { return ovStealth; }
    public void setOvStealth(String ovStealth) { this.ovStealth = ovStealth; }

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
                ", countryCode='" + countryCode + '\'' +
                ", regionName='" + regionName + '\'' +
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
