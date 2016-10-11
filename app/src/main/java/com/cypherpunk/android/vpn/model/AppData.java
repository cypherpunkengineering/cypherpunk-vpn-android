package com.cypherpunk.android.vpn.model;

import android.graphics.drawable.Drawable;


public class AppData {

    public final String name;
    public final Drawable icon;
    public final String packageName;
    public boolean check = true;

    public AppData(String name, Drawable icon, String packageName) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
    }
}
