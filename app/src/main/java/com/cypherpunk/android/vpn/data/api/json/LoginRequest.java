package com.cypherpunk.android.vpn.data.api.json;


public class LoginRequest {

    private final String login;

    private final String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}