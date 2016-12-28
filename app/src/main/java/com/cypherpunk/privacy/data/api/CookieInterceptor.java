package com.cypherpunk.privacy.data.api;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class CookieInterceptor implements Interceptor {

    private static final String COOKIE_HEADER_KEY = "Cookie";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!TextUtils.isEmpty(UserManager.getCookie())) {
            request = request.newBuilder()
                    .addHeader(COOKIE_HEADER_KEY, UserManager.getCookie())
                    .build();
        }
        Response response = chain.proceed(request);
        if (!response.headers("set-cookie").isEmpty()) {
            for (String header : response.headers("set-cookie")) {
                String cookie = header.split(";")[0];
                UserManager.saveCookie(cookie);
            }
        }
        return response;
    }
}
