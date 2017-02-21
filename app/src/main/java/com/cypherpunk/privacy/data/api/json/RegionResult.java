package com.cypherpunk.privacy.data.api.json;


public class RegionResult
{
    private String id;

    private String name;

    private String country;

    private String region;

    private String level;

    private boolean authorized;

    private String ovHostname;

    private String[] ovDefault;

    private String[] ovNone;

    private String[] ovStrong;

    private String[] ovStealth;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getOvHostname() {
        return ovHostname;
    }

    public void setOvHostname(String ovHostname) {
        this.ovHostname = ovHostname;
    }

    public String[] getOvDefault() {
        return ovDefault;
    }

    public void setOvDefault(String[] ovDefault) {
        this.ovDefault = ovDefault;
    }

    public String[] getOvNone() {
        return ovNone;
    }

    public void setOvNone(String[] ovNone) {
        this.ovNone = ovNone;
    }

    public String[] getOvStrong() {
        return ovStrong;
    }

    public void setOvStrong(String[] ovStrong) {
        this.ovStrong = ovStrong;
    }

    public String[] getOvStealth() {
        return ovStealth;
    }

    public void setOvStealth(String[] ovStealth) {
        this.ovStealth = ovStealth;
    }
}
