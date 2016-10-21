package com.cypherpunk.android.vpn.data.api;

import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.data.api.json.LoginResult;
import com.cypherpunk.android.vpn.data.api.json.RegionResult;
import com.cypherpunk.android.vpn.data.api.json.StatusResult;

import java.util.Map;

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

    @GET("/api/subscription/status")
    Single<StatusResult> getStatus();

    @GET("/api/vpn/serverList")
    Single<Map<String, Map<String, RegionResult[]>>> serverList();
}
