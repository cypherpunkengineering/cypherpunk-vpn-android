package com.cypherpunk.privacy.domain.repository.retrofit.requst;

import android.support.annotation.NonNull;

public class SignUpRequest {

    @NonNull
    private final String email;

    @NonNull
    private final String password;

    public SignUpRequest(@NonNull String email, @NonNull String password) {
        this.email = email;
        this.password = password;
    }
}
