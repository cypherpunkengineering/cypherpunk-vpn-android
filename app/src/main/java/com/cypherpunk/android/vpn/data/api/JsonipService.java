package com.cypherpunk.android.vpn.data.api;

import com.cypherpunk.android.vpn.data.api.json.JsonipResult;

import retrofit2.http.GET;
import rx.Single;

/**
 * jsonip API
 */
public interface JsonipService {

    String ENDPOINT = "http://jsonip.com";

    @GET("/")
    Single<JsonipResult> getIpAddress();
}
