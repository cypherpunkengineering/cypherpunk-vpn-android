package com.cypherpunk.privacy.data.api.json;


public class ChangeEmailRequest {

    private final String newEmail;

    private final String password;

    public ChangeEmailRequest(String newEmail, String password) {
        this.newEmail = newEmail;
        this.password = password;
    }
}
