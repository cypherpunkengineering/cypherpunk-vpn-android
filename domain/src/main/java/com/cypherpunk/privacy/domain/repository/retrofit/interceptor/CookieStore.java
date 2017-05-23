package com.cypherpunk.privacy.domain.repository.retrofit.interceptor;

import android.support.annotation.Nullable;

import com.orhanobut.hawk.Hawk;

/**
 * Store for cookie
 */
public class CookieStore {

    private static final String PREF_KEY_COOKIE = "cookie";

    @Nullable
    static String cookie() {
        return Hawk.get(PREF_KEY_COOKIE);
    }

    static void updateCookie(@Nullable String cookie) {
        Hawk.put(PREF_KEY_COOKIE, cookie);
    }

    public static void clear() {
        Hawk.remove(PREF_KEY_COOKIE);
    }
}
