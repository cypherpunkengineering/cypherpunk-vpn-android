package com.cypherpunk.privacy.datasource.vpn;

import android.support.annotation.NonNull;

public enum Region {
    DEVELOP("DEV"),
    NORTH_AMERICA("NA"),
    SOUTH_AMERICA("SA"),
    // TODO
    CR("CR"),
    EUROPE("EU"),
    // TODO
    ME("ME"),
    // TODO
    AF("AF"),
    ASIA("AS"),
    OCEANIA_PACIFIC("OP");

    @NonNull
    private final String value;

    Region(@NonNull String value) {
        this.value = value;
    }

    @NonNull
    public String value() {
        return value;
    }
}
