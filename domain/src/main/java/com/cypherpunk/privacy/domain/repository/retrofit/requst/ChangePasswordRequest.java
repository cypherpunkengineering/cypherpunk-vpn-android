package com.cypherpunk.privacy.domain.repository.retrofit.requst;

import android.support.annotation.NonNull;

public class ChangePasswordRequest {

    @NonNull
    private final String oldPassword;

    @NonNull
    private final String newPassword;

    public ChangePasswordRequest(@NonNull String oldPassword, @NonNull String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
