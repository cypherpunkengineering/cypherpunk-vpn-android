package com.cypherpunk.privacy.data.api;

import com.cypherpunk.privacy.data.api.json.ChangeEmailRequest;
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
import rx.Single;

/**
 * Cypherpunk API
 */
public interface CypherpunkService {
    String ENDPOINT = "https://cypherpunk.engineering";

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

    @GET("/api/v0/vpn/serverList")
    Single<Map<String, Map<String, RegionResult[]>>> serverList();

    @POST("/api/v0/account/email/change")
    Single<ResponseBody> changeEmail(
            @Body ChangeEmailRequest identifyEmailRequest);

    @POST("/api/v1/account/password/recover")
    Single<ResponseBody> recoverPassword(
            @Body EmailRequest emailRequest);
}
