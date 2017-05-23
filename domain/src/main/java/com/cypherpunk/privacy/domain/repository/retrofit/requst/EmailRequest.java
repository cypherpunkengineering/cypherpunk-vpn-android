package com.cypherpunk.privacy.domain.repository.retrofit.requst;

import android.support.annotation.NonNull;

public class EmailRequest {

    @NonNull
    private final String email;

    public EmailRequest(@NonNull String email) {
        this.email = email;
    }
}
