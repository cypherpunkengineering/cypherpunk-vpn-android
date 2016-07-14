package com.cypherpunk.android.vpn.data.api;

import com.cypherpunk.android.vpn.data.api.model.LoginRequest;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Cypherpunk API
 */
public interface CypherpunkService {

    String ENDPOINT = "https://cypherpunk.engineering/";

    @POST("account/authenticate/userpasswd")
    Observable<ResponseBody> login(
            @Body LoginRequest loginRequest);

    @GET("api/vpn/serverList")
    Observable<ResponseBody> serverList();
}
