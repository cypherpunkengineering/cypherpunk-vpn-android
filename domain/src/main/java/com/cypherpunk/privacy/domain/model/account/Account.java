package com.cypherpunk.privacy.domain.model.account;

import android.support.annotation.NonNull;

/**
 * account information
 */
public class Account {

    @NonNull
    private final String id;
    @NonNull
    private final String email;
    @NonNull
    private final Type type;
    private final boolean confirmed;

    public Account(@NonNull String id, @NonNull String email, @NonNull Type type,
                   boolean confirmed) {
        this.id = id;
        this.email = email;
        this.type = type;
        this.confirmed = confirmed;
    }

    @NonNull
    public String id() {
        return id;
    }

    @NonNull
    public String email() {
        return email;
    }

    @NonNull
    public Type type() {
        return type;
    }

    public boolean confirmed() {
        return confirmed;
    }

    public enum Type {
        FREE("free"),
        PREMIUM("premium"),
        ORGANIZATION("organization"),
        DEVELOPER("developer");

        @NonNull
        private final String value;

        Type(@NonNull String value) {
            this.value = value;
        }

        @NonNull
        public static Type find(String value) {
            for (Type accountType : values()) {
                if (accountType.value.equals(value)) {
                    return accountType;
                }
            }
            return FREE;
        }

        @NonNull
        public String value() {
            return value;
        }
    }
}
