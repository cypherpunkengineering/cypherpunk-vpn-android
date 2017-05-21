package com.cypherpunk.privacy.domain.repository.retrofit.interceptor;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

    private static final String USER_AGENT_HEADER_KEY = "User-Agent";

    private final String userAgent;

    public UserAgentInterceptor(@NonNull String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        final Request request;
        if (!TextUtils.isEmpty(userAgent)) {
            request = chain.request()
                    .newBuilder()
                    .addHeader(USER_AGENT_HEADER_KEY, userAgent)
                    .build();
        } else {
            request = chain.request();
        }
        return chain.proceed(request);
    }
}
