package com.cypherpunk.android.vpn.data.api.json;


public class LocationResult
{
    private String city;

    private String commonName;

    private String ipDefault;

    private String ipNone;

    private String ipStrong;

    private String ipStealth;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getIpDefault() {
        return ipDefault;
    }

    public void setIpDefault(String ipDefault) {
        this.ipDefault = ipDefault;
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

    public String getIpStealth() {
        return ipStealth;
    }

    public void setIpStealth(String ipStealth) {
        this.ipStealth = ipStealth;
    }
}
