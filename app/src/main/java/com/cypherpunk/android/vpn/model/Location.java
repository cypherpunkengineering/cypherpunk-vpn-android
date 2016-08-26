package com.cypherpunk.android.vpn.model;


import java.io.Serializable;

public class Location implements Serializable {

    private String name;

    private String ipAddress;

    private boolean favorite;

    private int mapX;

    private int mapY;

    public Location(String name, String ipAddress, int mapX, int mapY) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {

        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

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
