package com.cypherpunk.privacy.data.api.json;


public class SignUpRequest {

    private final String email;

    private final String password;

    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
