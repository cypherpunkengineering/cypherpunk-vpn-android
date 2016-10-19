package com.cypherpunk.android.vpn.model;

import android.support.annotation.NonNull;
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass
public class Location implements RealmModel {

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

    @Required
    private String nationalFlagUrl;
    private boolean favorited;
    private boolean selected;

    @SuppressWarnings("unused")
    public Location() {
    }

    public Location
    (
         @NonNull String id,
         @NonNull String countryCode,
         @NonNull String regionName,

         @NonNull String ovHostname,

         @NonNull String ovDefault,
         @NonNull String ovNone,
         @NonNull String ovStrong,
         @NonNull String ovStealth,

         @NonNull String nationalFlagUrl
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

        this.nationalFlagUrl = nationalFlagUrl;
    }

    public String getId() { return id; }

    public String getCountryCode() { return countryCode; }
    public String getRegionName() { return regionName; }

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

    public String getNationalFlagUrl() { return nationalFlagUrl; }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
                ", nationalFlagUrl='" + nationalFlagUrl + '\'' +
                ", favorited=" + favorited +
                ", selected=" + selected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;

        final Location location = (Location) o;
        return id != null ? id.equals(location.id) : location.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
