package com.cypherpunk.privacy.domain.model;

import android.support.annotation.NonNull;

/**
 * type of account
 */
public enum AccountType {
    FREE("free"),
    PREMIUM("premium"),
    ORGANIZATION("organization"),
    DEVELOPER("developer");

    @NonNull
    private final String value;

    AccountType(@NonNull String value) {
        this.value = value;
    }

    @NonNull
    public static AccountType find(String value) {
        for (AccountType accountType : values()) {
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
