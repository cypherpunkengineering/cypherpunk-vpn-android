package com.cypherpunk.privacy.domain.model.vpn;

import android.support.annotation.NonNull;

public enum Level {
    PREMIUM("premium"),
    DEVELOPER("developer"),
    UNAVAILABLE("unavailable");

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
        return UNAVAILABLE;
    }
}
