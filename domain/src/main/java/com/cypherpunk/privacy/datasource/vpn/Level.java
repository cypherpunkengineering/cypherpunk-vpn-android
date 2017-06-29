package com.cypherpunk.privacy.datasource.vpn;

import android.support.annotation.NonNull;

public enum Level {
    PREMIUM("premium"),
    DEVELOPER("developer"),
    FREE("free");

    @NonNull
    private final String value;

    private Level(@NonNull String value) {
        this.value = value;
    }

    @NonNull
    public String value() {
        return value;
    }

    @NonNull
    public static Level find(String value) {
        for (Level level : values()) {
            if (level.value().equals(value)) {
                return level;
            }
        }
        return FREE;
    }
}
