package com.cypherpunk.privacy.domain.repository.retrofit;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.domain.repository.retrofit.requst.ChangeEmailRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.ChangePasswordRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.EmailRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.LoginRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.SignUpRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.UpgradeAccountRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Cypherpunk API
 */
public interface CypherpunkService {
    String ENDPOINT = "https://cypherpunk.privacy.network";

    @POST("/api/v0/account/identify/email")
    Completable identifyEmail(@NonNull EmailRequest request);

    @POST("/api/v0/account/register/signUp")
    Single<StatusResult> signUp(@NonNull SignUpRequest request);

    @POST("/api/v0/account/email/confirm")
    Completable resendEmail(@NonNull EmailRequest request);

    @POST("/api/v0/account/authenticate/userpasswd")
    Single<StatusResult> login(@NonNull LoginRequest request);

    @POST("/api/v0/account/password/recover")
    Completable recoverPassword(@NonNull EmailRequest request);

    @POST("/api/v0/account/email/change")
    Completable changeEmail(@NonNull ChangeEmailRequest request);

    @POST("/api/v0/account/password/change")
    Completable changePassword(@NonNull ChangePasswordRequest request);

    @GET("/api/v0/account/status")
    Single<StatusResult> getAccountStatus();

    @POST("/api/v0/account/upgrade/GooglePlay")
    Single<StatusResult> upgradeAccount(@NonNull UpgradeAccountRequest request);

    @GET("/api/v0/location/list/{accountType}")
    Single<Map<String, RegionResult>> serverList(@Path(value = "accountType", encoded = true) String accountType);
}
