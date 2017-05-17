package com.cypherpunk.privacy.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.auto.value.AutoValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * subscription plan
 */
@AutoValue
public abstract class Plan {

    @NonNull
    public static Plan create(@NonNull Renewal renewal, @Nullable Date expiration) {
        return new AutoValue_Plan(renewal, expiration);
    }

    @NonNull
    public abstract Renewal renewal();

    @Nullable
    public abstract Date expiration();

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

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Nullable
    public static Date toDate(@Nullable String expirationText) {
        if (!TextUtils.isEmpty(expirationText)) {
            final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault());
            try {
                return sdf.parse(expirationText);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
