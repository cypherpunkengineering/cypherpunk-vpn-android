package com.cypherpunk.privacy.domain.repository.retrofit.interceptor;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CookieInterceptor implements Interceptor {

    private static final String COOKIE_HEADER_KEY = "Cookie";
    private static final String SET_COOKIE_HEADER_KEY = "set-cookie";

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        final String cookie = CookieStore.cookie();

        final Request request;
        if (!TextUtils.isEmpty(cookie)) {
            request = chain.request()
                    .newBuilder()
                    .addHeader(COOKIE_HEADER_KEY, cookie)
                    .build();
        } else {
            request = chain.request();
        }

        final Response response = chain.proceed(request);

        for (String header : response.headers(SET_COOKIE_HEADER_KEY)) {
            CookieStore.updateCookie(header.split(";")[0]);
        }
        return response;
    }
}
