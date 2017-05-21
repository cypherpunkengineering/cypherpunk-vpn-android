package com.cypherpunk.privacy.domain.model;

/**
 * privacy info
 */
public class Privacy {
    private final String username;
    private final String password;

    public Privacy(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }
}
