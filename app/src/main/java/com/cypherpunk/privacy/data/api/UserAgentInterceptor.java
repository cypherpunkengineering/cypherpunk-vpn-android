package com.cypherpunk.privacy.data.api;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class UserAgentInterceptor implements Interceptor {

    private static final String USER_AGENT_HEADER_KEY = "User-Agent";

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (!TextUtils.isEmpty(userAgent)) {
            request = request.newBuilder()
                    .addHeader(USER_AGENT_HEADER_KEY, userAgent)
                    .build();
        }
        return chain.proceed(request);
    }
}
