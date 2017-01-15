package com.cypherpunk.privacy.data.api.json;


public class ChangePasswordRequest {

    private final String oldPassword;

    private final String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
