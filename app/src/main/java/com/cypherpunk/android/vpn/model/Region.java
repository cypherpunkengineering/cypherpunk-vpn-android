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
    private String area;
    @Required
    private String countryCode;
    @Required
    private String city;
    @Required
    private String ipAddress;

    @SuppressWarnings("unused")
    public Region() {
    }

    public Region(@NonNull String area,
                  @NonNull String countryCode,
                  @NonNull String city,
                  @NonNull String ipAddress) {
        this.area = area;
        this.countryCode = countryCode;
        this.city = city;
        this.ipAddress = ipAddress;

        id = calcId(countryCode, area, city);
    }

    public String getId() {
        return id;
    }

    public String getArea() {
        return area;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCity() {
        return city;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String toString() {
        return "Region{" +
                "id='" + id + '\'' +
                ", area='" + area + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", city='" + city + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
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

    private static String calcId(String countryCode, String area, String city) {
        final String str = area + "##" + countryCode + "##" + city;

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
