package com.cypherpunk.privacy.datasource.vpn;

import android.support.annotation.NonNull;

/**
 * setting kind for internet kill switch
 */
public enum InternetKillSwitch {
    AUTOMATIC("auto"),
    OFF("off"),
    ALWAYS_ON("always");

    @NonNull
    private final String value;

    InternetKillSwitch(@NonNull String value) {
        this.value = value;
    }

    @NonNull
    public static InternetKillSwitch find(String value) {
        for (InternetKillSwitch internetKillSwitch : values()) {
            if (internetKillSwitch.value.equals(value)) {
                return internetKillSwitch;
            }
        }
        return OFF;
    }

    @NonNull
    public String value() {
        return value;
    }
}
