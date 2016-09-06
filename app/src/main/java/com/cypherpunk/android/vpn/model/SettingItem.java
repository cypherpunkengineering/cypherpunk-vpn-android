package com.cypherpunk.android.vpn.model;

import java.io.Serializable;

public class SettingItem implements Serializable {

    public final String title;
    public final String description;
    public final String value;

    public SettingItem(String title, String description, String value) {
        this.title = title;
        this.description = description;
        this.value = value;
    }
}
