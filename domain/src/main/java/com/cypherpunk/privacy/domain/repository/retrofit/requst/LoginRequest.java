package com.cypherpunk.privacy.domain.repository.retrofit.requst;

import android.support.annotation.NonNull;

public class LoginRequest {

    @NonNull
    private final String login;

    @NonNull
    private final String password;

    public LoginRequest(@NonNull String login, @NonNull String password) {
        this.login = login;
        this.password = password;
    }
}
