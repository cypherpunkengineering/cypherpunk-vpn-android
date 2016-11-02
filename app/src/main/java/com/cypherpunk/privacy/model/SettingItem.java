package com.cypherpunk.privacy.model;

import java.io.Serializable;

public class SettingItem implements Serializable {

    public final String key;
    public final String value;
    public final String description;

    public final String cipher;
    public final String auth;
    public final String keylen;

    public SettingItem(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;

        this.cipher = null;
        this.auth = null;
        this.keylen = null;
    }

    public SettingItem(String key, String value, String description, String cipher, String auth, String keylen) {
        this.key = key;
        this.value = value;
        this.description = description;

        this.cipher = cipher;
        this.auth = auth;
        this.keylen = keylen;
    }
}
