package com.cypherpunk.privacy.datasource.account;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * subscription plan
 */
public class Subscription {

    @NonNull
    private final Type type;
    @Nullable
    private final Date expiration;
    private final boolean active;
    private final boolean renews;

    public Subscription(@NonNull Type type, @Nullable Date expiration, boolean active, boolean renews) {
        this.type = type;
        this.expiration = expiration;
        this.active = active;
        this.renews = renews;
    }

    @NonNull
    public Type type() {
        return type;
    }

    @Nullable
    public Date expiration() {
        return expiration;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRenews() {
        return renews;
    }

    public enum Type {
        NONE("none"),
        TRIAL("trial"),
        DAILY("daily"),
        MONTHLY("monthly"),
        SEMIANNUALLY("semiannually"),
        ANNUALLY("annually"),
        FOREVER("forever");

        @NonNull
        private final String value;

        Type(@NonNull String value) {
            this.value = value;
        }

        @NonNull
        public static Type find(String value) {
            for (Type type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return NONE;
        }

        @NonNull
        public String value() {
            return value;
        }
    }
}
