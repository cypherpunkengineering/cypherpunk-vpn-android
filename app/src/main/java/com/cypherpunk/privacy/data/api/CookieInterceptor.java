package com.cypherpunk.privacy.data.api;

import android.text.TextUtils;

import com.cypherpunk.privacy.model.UserSetting;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CookieInterceptor implements Interceptor {

    private static final String COOKIE_HEADER_KEY = "Cookie";

    @Override
    public Response intercept(Chain chain) throws IOException {
        final UserSetting userSetting = UserSetting.instance();

        final Request request;
        final String cookie = UserSetting.instance().cookie();
        if (!TextUtils.isEmpty(cookie)) {
            request = chain.request()
                    .newBuilder()
                    .addHeader(COOKIE_HEADER_KEY, cookie)
                    .build();
        } else {
            request = chain.request();
        }

        final Response response = chain.proceed(request);

        for (String header : response.headers("set-cookie")) {
            userSetting.updateCookie(header.split(";")[0]);
        }
        return response;
    }
}
