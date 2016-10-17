package com.cypherpunk.android.vpn.data.api.json;


public class LocationResult {

    private String ipStealth;

    private String ipNone;

    private String ipStrong;

    private String ipDefault;

    private String city;

    public String getIpStealth() {
        return ipStealth;
    }

    public void setIpStealth(String ipStealth) {
        this.ipStealth = ipStealth;
    }

    public String getIpNone() {
        return ipNone;
    }

    public void setIpNone(String ipNone) {
        this.ipNone = ipNone;
    }

    public String getIpStrong() {
        return ipStrong;
    }

    public void setIpStrong(String ipStrong) {
        this.ipStrong = ipStrong;
    }

    public String getIpDefault() {
        return ipDefault;
    }

    public void setIpDefault(String ipDefault) {
        this.ipDefault = ipDefault;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
