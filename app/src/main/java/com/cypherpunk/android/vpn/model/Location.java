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
    private static Charset s = Charset.forName("UTF-8");

    @PrimaryKey
    @Required
    private String id;

    @Required
    private String city;
    @Required
    private String countryCode;

    @Required
    private String hostname;

    @Required
    private String ipDefault;
    @Required
    private String ipNone;
    @Required
    private String ipStrong;
    @Required
    private String ipStealth;

    @Required
    private String nationalFlagUrl;
    private boolean favorited;
    private boolean selected;

    private int mapX;
    private int mapY;

    @SuppressWarnings("unused")
    public Location() {
    }

    public Location
    (
         @NonNull String city,
         @NonNull String countryCode,

         @NonNull String hostname,

         @NonNull String ipDefault,
         @NonNull String ipNone,
         @NonNull String ipStrong,
         @NonNull String ipStealth,

         @NonNull String nationalFlagUrl,

         int mapX,
         int mapY
    )
    {
        this.city = city;
        this.countryCode = countryCode;

        this.hostname = hostname;

        this.ipDefault = ipDefault;
        this.ipNone = ipNone;
        this.ipStrong = ipStrong;
        this.ipStealth = ipStealth;

        this.nationalFlagUrl = nationalFlagUrl;

        this.mapX = mapX;
        this.mapY = mapY;

        id = calcId(countryCode, city);
    }

    public String getId() { return id; }

    public String getCity() { return city; }
    public String getCountryCode() { return countryCode; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getIpDefault() { return ipDefault; }
    public void setIpDefault(String ipDefault) { this.ipDefault = ipDefault; }
    public String getIpNone() { return ipNone; }
    public void setIpNone(String ipNone) { this.ipNone = ipNone; }
    public String getIpStrong() { return ipStrong; }
    public void setIpStrong(String ipStrong) { this.ipStrong = ipStrong; }
    public String getIpStealth() { return ipStealth; }
    public void setIpStealth(String ipStealth) { this.ipStealth = ipStealth; }

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

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    @Override
    public String toString() {
        return "Region{" +
                "id='" + id + '\'' +
                ", city='" + city + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", ipDefault='" + ipDefault + '\'' +
                ", ipNone='" + ipNone + '\'' +
                ", ipStrong='" + ipStrong + '\'' +
                ", ipStealth='" + ipStealth + '\'' +
                ", nationalFlagUrl='" + nationalFlagUrl + '\'' +
                ", favorited=" + favorited +
                ", selected=" + selected +
                ", mapX=" + mapX +
                ", mapY=" + mapY +
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

    private static String calcId(String countryCode, String city) {
        final String str = countryCode + "##" + city;

        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            final byte[] digestBytes = md.digest(str.getBytes(s));
            return Base64.encodeToString(digestBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
