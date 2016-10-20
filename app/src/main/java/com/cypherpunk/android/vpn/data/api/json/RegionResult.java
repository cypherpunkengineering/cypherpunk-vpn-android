package com.cypherpunk.android.vpn.data.api.json;


public class RegionResult
{
    private String id;

    private String regionName;

    private String ovHostname;

    private String ovDefault;

    private String ovNone;

    private String ovStrong;

    private String ovStealth;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getOvHostname() {
        return ovHostname;
    }

    public void setOvHostname(String ovHostname) {
        this.ovHostname = ovHostname;
    }

    public String getOvDefault() {
        return ovDefault;
    }

    public void setOvDefault(String ovDefault) {
        this.ovDefault = ovDefault;
    }

    public String getOvNone() {
        return ovNone;
    }

    public void setOvNone(String ovNone) {
        this.ovNone = ovNone;
    }

    public String getOvStrong() {
        return ovStrong;
    }

    public void setOvStrong(String ovStrong) {
        this.ovStrong = ovStrong;
    }

    public String getOvStealth() {
        return ovStealth;
    }

    public void setOvStealth(String ovStealth) {
        this.ovStealth = ovStealth;
    }
}
