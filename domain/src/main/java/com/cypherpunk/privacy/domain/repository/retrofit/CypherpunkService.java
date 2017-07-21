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
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Cypherpunk API
 */
public interface CypherpunkService {
    String ENDPOINT = "https://api.cypherpunk.com";

    @POST("/api/v1/account/identify/email")
    Completable identifyEmail(@Body @NonNull EmailRequest request);

    @POST("/api/v1/account/register/signup")
    Single<StatusResult> signUp(@Body @NonNull SignUpRequest request);

    @POST("/api/v1/account/recover/email")
    Completable resendEmail(@Body @NonNull EmailRequest request);

    @POST("/api/v1/account/authenticate/userpasswd")
    Single<StatusResult> login(@Body @NonNull LoginRequest request);

    @POST("/api/v1/account/recover")
    Completable recoverPassword(@Body @NonNull EmailRequest request);

    @POST("/api/v1/account/email/change")
    Completable changeEmail(@Body @NonNull ChangeEmailRequest request);

    @POST("/api/v1/account/password/change")
    Completable changePassword(@Body @NonNull ChangePasswordRequest request);

    @GET("/api/v1/account/status")
    Single<StatusResult> getAccountStatus();

    @POST("/api/v1/account/upgrade/google")
    Single<StatusResult> upgradeAccount(@Body @NonNull UpgradeAccountRequest request);

    @GET("/api/v1/location/list/{accountType}")
    Single<Map<String, RegionResult>> serverList(@Path(value = "accountType", encoded = true) String accountType);
}
