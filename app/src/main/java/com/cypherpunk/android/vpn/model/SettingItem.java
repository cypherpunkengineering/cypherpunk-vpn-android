package com.cypherpunk.android.vpn.model;

import java.io.Serializable;

public class SettingItem implements Serializable {

    public final String value;
    public final String description;

    public SettingItem(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
