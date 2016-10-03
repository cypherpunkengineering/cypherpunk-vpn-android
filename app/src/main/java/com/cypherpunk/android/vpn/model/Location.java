package com.cypherpunk.android.vpn.model;


import java.io.Serializable;

public class Location implements Serializable {

    private String name;

    private String ipDefault;
    private String ipNone;
    private String ipStrong;
    private String ipStealth;

    private boolean favorite;

    private int mapX;

    private int mapY;

    public Location(String name, int mapX, int mapY, String ipDefault, String ipNone, String ipStrong, String ipStealth) {
        this.name = name;
        this.ipDefault = ipDefault;
        this.ipNone = ipNone;
        this.ipStrong = ipStrong;
        this.ipStealth = ipStealth;
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpDefault() { return ipDefault; }
    public String getIpNone() { return ipNone; }
    public String getIpStrong() { return ipStrong; }
    public String getIpStealth() { return ipStealth; }

    public void setIpDefault(String ipDefault) { this.ipDefault = ipDefault; }
    public void setIpNone(String ipNone) { this.ipNone = ipNone; }
    public void setIpStrong(String ipStrong) { this.ipStrong = ipStrong; }
    public void setIpStealth(String ipStealth) { this.ipStealth = ipStealth; }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getMapX() {
        return mapX;
    }

    public void setMapX(int mapX) {
        this.mapX = mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public void setMapY(int mapY) {
        this.mapY = mapY;
    }
}
