package com.cypherpunk.android.privacy.data.api;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;


public class CookieManager implements CookieJar {

    private final List<Cookie> saveCookies = new ArrayList<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        for (Cookie c : cookies) {
            if (!saveCookies.contains(c)) {
                saveCookies.add(c);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return saveCookies;
    }
}
