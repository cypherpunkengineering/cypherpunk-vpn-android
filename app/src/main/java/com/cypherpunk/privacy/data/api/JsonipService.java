package com.cypherpunk.privacy.data.api;

import com.cypherpunk.privacy.data.api.json.JsonipResult;

import retrofit2.http.GET;
import rx.Single;

/**
 * jsonip API
 */
public interface JsonipService {

    String ENDPOINT = "https://jsonip.com";

    @GET("/")
    Single<JsonipResult> getIpAddress();
}