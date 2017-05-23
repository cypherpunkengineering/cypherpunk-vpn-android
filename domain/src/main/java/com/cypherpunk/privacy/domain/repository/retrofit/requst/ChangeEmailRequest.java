package com.cypherpunk.privacy.domain.repository.retrofit.requst;

import android.support.annotation.NonNull;

public class ChangeEmailRequest {

    @NonNull
    private final String newEmail;

    @NonNull
    private final String password;

    public ChangeEmailRequest(@NonNull String newEmail, @NonNull String password) {
        this.newEmail = newEmail;
        this.password = password;
    }
}
