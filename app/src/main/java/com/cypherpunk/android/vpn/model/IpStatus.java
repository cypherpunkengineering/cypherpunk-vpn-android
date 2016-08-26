package com.cypherpunk.android.vpn.model;

import android.os.Parcel;
import android.os.Parcelable;

public class IpStatus implements Parcelable {

    private String originalIp;

    private String newIp;

    private String location;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.originalIp);
        dest.writeString(this.newIp);
        dest.writeString(this.location);
    }

    public IpStatus() {
    }

    protected IpStatus(Parcel in) {
        this.originalIp = in.readString();
        this.newIp = in.readString();
        this.location = in.readString();
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
