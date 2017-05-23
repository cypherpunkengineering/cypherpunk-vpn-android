package com.cypherpunk.privacy.domain.repository;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.domain.model.Account;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface NetworkRepository {

    @NonNull
    Completable identifyEmail(@NonNull String email);

    @NonNull
    Single<StatusResult> signUp(@NonNull String email, @NonNull String password);

    @NonNull
    Completable resendEmail(@NonNull String email);

    @NonNull
    Single<StatusResult> login(@NonNull String email, @NonNull String password);

    @NonNull
    Completable recoverPassword(@NonNull String email);

    @NonNull
    Completable changeEmail(@NonNull String email, @NonNull String password);

    @NonNull
    Completable changePassword(@NonNull String oldPassword, @NonNull String newPassword);

    @NonNull
    Single<StatusResult> getAccountStatus();

    @NonNull
    Single<StatusResult> upgradeAccount(String accountId, @NonNull String sku, String purchaseJson);

    @NonNull
    Single<Map<String, RegionResult>> serverList(@NonNull Account.Type type);
}
