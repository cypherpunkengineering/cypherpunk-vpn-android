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
public class Region implements RealmModel {
    private static Charset s = Charset.forName("UTF-8");

    @PrimaryKey
    @Required
    private String id;

    @Required
    private String city;
    @Required
    private String countryCode;
    @Required
    private String ipAddress;
    @Required
    private String nationalFlagUrl;
    private boolean favorited;
    private boolean selected;
    private int mapX;
    private int mapY;

    @SuppressWarnings("unused")
    public Region() {
    }

    public Region(
            @NonNull String city,
            @NonNull String countryCode,
            @NonNull String ipAddress,
            @NonNull String nationalFlagUrl,
            int mapX,
            int mapY) {
        this.city = city;
        this.countryCode = countryCode;
        this.ipAddress = ipAddress;
        this.nationalFlagUrl = nationalFlagUrl;
        this.mapX = mapX;
        this.mapY = mapY;

        id = calcId(countryCode, city);
    }

    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getNationalFlagUrl() {
        return nationalFlagUrl;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isSelected() {
        return selected;
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
                ", ipAddress='" + ipAddress + '\'' +
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
        if (!(o instanceof Region)) return false;

        final Region region = (Region) o;
        return id != null ? id.equals(region.id) : region.id == null;
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
