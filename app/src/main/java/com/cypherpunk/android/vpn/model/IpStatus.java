package com.cypherpunk.android.vpn.model;

import android.os.Parcel;
import android.os.Parcelable;

public class IpStatus implements Parcelable {

    private String originalIp;

    private String newIp;

    private String location;

    private int mapX;

    private int mapY;

    public String getOriginalIp() {
        return originalIp;
    }

    public void setOriginalIp(String originalIp) {
        this.originalIp = originalIp;
    }

    public String getNewIp() {
        return newIp;
    }

    public void setNewIp(String newIp) {
        this.newIp = newIp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public void setMapPosition(int x, int y) {
        mapX = x;
        mapY = y;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.originalIp);
        dest.writeString(this.newIp);
        dest.writeString(this.location);
        dest.writeInt(this.mapX);
        dest.writeInt(this.mapY);
    }

    public IpStatus() {
    }

    protected IpStatus(Parcel in) {
        this.originalIp = in.readString();
        this.newIp = in.readString();
        this.location = in.readString();
        this.mapX = in.readInt();
        this.mapY = in.readInt();
    }

    public static final Parcelable.Creator<IpStatus> CREATOR = new Parcelable.Creator<IpStatus>() {
        public IpStatus createFromParcel(Parcel source) {
            return new IpStatus(source);
        }

        public IpStatus[] newArray(int size) {
            return new IpStatus[size];
        }
    };
}