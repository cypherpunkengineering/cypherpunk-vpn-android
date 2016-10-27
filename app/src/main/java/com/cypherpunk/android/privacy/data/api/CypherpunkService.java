package com.cypherpunk.android.privacy.data.api;

import com.cypherpunk.android.privacy.data.api.json.IdentifyEmailRequest;
import com.cypherpunk.android.privacy.data.api.json.LoginRequest;
import com.cypherpunk.android.privacy.data.api.json.LoginResult;
import com.cypherpunk.android.privacy.data.api.json.RegionResult;
import com.cypherpunk.android.privacy.data.api.json.SignUpRequest;
import com.cypherpunk.android.privacy.data.api.json.StatusResult;

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

    @POST("/account/authenticate/userpasswd")
    Single<LoginResult> login(
            @Body LoginRequest loginRequest);

    @POST("/account/register/signup")
    Single<LoginResult> signup(
            @Body SignUpRequest loginRequest);

    @POST("/account/identify/email")
    Single<ResponseBody> identifyEmail(
            @Body IdentifyEmailRequest identifyEmailRequest);

    @GET("/api/subscription/status")
    Single<StatusResult> getStatus();

    @GET("/api/vpn/serverList")
    Single<Map<String, Map<String, RegionResult[]>>> serverList();
}
