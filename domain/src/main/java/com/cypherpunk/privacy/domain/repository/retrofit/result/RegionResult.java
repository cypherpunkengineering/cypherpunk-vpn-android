package com.cypherpunk.privacy.domain.repository.retrofit.result;

public class RegionResult {
    public final String id;
    public final String name;
    public final String country;
    public final String region;
    public final String level;
    public final boolean authorized;
    public final String ovHostname;
    public final String[] ovDefault;
    public final String[] ovNone;
    public final String[] ovStrong;
    public final String[] ovStealth;

    public RegionResult(String id, String name, String country, String region, String level,
                        boolean authorized, String ovHostname, String[] ovDefault, String[] ovNone,
                        String[] ovStrong, String[] ovStealth) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.region = region;
        this.level = level;
        this.authorized = authorized;
        this.ovHostname = ovHostname;
        this.ovDefault = ovDefault;
        this.ovNone = ovNone;
        this.ovStrong = ovStrong;
        this.ovStealth = ovStealth;
    }
}
