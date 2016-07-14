package com.cypherpunk.android.vpn;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthInterceptor implements Interceptor {

    private final String token;

    public AuthInterceptor(String accessToken) {
        this.token = accessToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader("Cookie", "wiz.session=" + token)
                .build();
        return chain.proceed(request);
    }
}
