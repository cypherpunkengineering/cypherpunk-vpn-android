package com.cypherpunk.privacy.domain.model.account;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * subscription plan
 */
public class Subscription {

    @NonNull
    private final Renewal renewal;
    @Nullable
    private final Date expiration;

    public Subscription(@NonNull Renewal renewal, @Nullable Date expiration) {
        this.renewal = renewal;
        this.expiration = expiration;
    }

    @NonNull
    public Renewal renewal() {
        return renewal;
    }

    @Nullable
    public Date expiration() {
        return expiration;
    }

    public enum Renewal {
        NONE("none"),
        MONTHLY("monthly"),
        ANNUALLY("annually"),
        FOREVER("forever"),
        LIFETIME("lifetime");

        @NonNull
        private final String value;

        Renewal(@NonNull String value) {
            this.value = value;
        }

        @NonNull
        public static Renewal find(String value) {
            for (Renewal renewal : values()) {
                if (renewal.value.equals(value)) {
                    return renewal;
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
