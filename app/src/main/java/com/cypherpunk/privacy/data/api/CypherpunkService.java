package com.cypherpunk.privacy.data.api;

import com.cypherpunk.privacy.data.api.json.ChangeEmailRequest;
import com.cypherpunk.privacy.data.api.json.ChangePasswordRequest;
import com.cypherpunk.privacy.data.api.json.EmailRequest;
import com.cypherpunk.privacy.data.api.json.LoginRequest;
import com.cypherpunk.privacy.data.api.json.LoginResult;
import com.cypherpunk.privacy.data.api.json.RegionResult;
import com.cypherpunk.privacy.data.api.json.SignUpRequest;
import com.cypherpunk.privacy.data.api.json.StatusResult;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import rx.Single;

/**
 * Cypherpunk API
 */
public interface CypherpunkService {
    String ENDPOINT = "https://cypherpunk.com";

    @POST("/api/v0/account/authenticate/userpasswd")
    Single<LoginResult> login(
            @Body LoginRequest loginRequest);

    @POST("/api/v0/account/register/signup")
    Single<LoginResult> signup(
            @Body SignUpRequest loginRequest);

    @POST("/api/v0/account/identify/email")
    Single<ResponseBody> identifyEmail(
            @Body EmailRequest emailRequest);

    @GET("/api/v0/subscription/status")
    Single<StatusResult> getStatus();

    @GET("/api/v0/subscription/status")
    Observable<StatusResult> getStatusObservable();

    @GET("/api/v0/location/list/{accountType}")
    Single<Map<String, RegionResult>> serverList(@Path(value = "accountType", encoded = true) String accountType);

    @POST("/api/v0/account/email/change")
    Single<ResponseBody> changeEmail(
            @Body ChangeEmailRequest changeEmailRequest);

    @POST("/api/v0/account/password/change")
    Single<ResponseBody> changePassword(
            @Body ChangePasswordRequest changePasswordRequest);

    @POST("/api/v0/account/password/recover")
    Single<ResponseBody> recoverPassword(
            @Body EmailRequest emailRequest);

    @POST("/api/v0/account/email/confirm")
    Single<ResponseBody> resendEmail(
            @Body EmailRequest emailRequest);
}
