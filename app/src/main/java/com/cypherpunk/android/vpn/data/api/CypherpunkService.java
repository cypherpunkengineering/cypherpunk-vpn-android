package com.cypherpunk.android.vpn.data.api;

import com.cypherpunk.android.vpn.data.api.json.RegionResult;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Single;

/**
 * Cypherpunk API
 */
public interface CypherpunkService
{
    String ENDPOINT = "https://cypherpunk.engineering";

    @POST("/account/authenticate/userpasswd")
    Single<ResponseBody> login(
            @Body LoginRequest loginRequest);

    @GET("/api/vpn/serverList")
    Single<Map<String, Map<String, RegionResult[]>>> serverList();
}
