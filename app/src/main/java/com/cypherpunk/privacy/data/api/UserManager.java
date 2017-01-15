package com.cypherpunk.privacy.data.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.model.UserSettingPref;
import com.orhanobut.hawk.Hawk;

public class UserManager {

    private static final String PREF_KEY_SECRET = "secret";
    private static final String PREF_KEY_COOKIE = "cookie";

    private static final String PREF_KEY_VPN_USERNAME = "vpn_username";
    private static final String PREF_KEY_VPN_PASSWORD = "vpn_password";

    public static boolean isSignedIn() {
        String mail = getMailAddress();
        return !TextUtils.isEmpty(mail);
    }

    @Nullable
    public static String getMailAddress() {
        return new UserSettingPref().mail;
    }

    public static void saveMailAddress(@NonNull String mail) {
        UserSettingPref user = new UserSettingPref();
        user.mail = mail;
        user.save();
    }

    @Nullable
    public static String getCookie() {
        return Hawk.get(PREF_KEY_COOKIE);
    }

    public static void saveCookie(@NonNull String cookie) {
        Hawk.put(PREF_KEY_COOKIE, cookie);
    }

    @Nullable
    public static String getSecret() {
        return Hawk.get(PREF_KEY_SECRET);
    }

    public static void saveSecret(@NonNull String secret) {
        Hawk.put(PREF_KEY_SECRET, secret);
    }

    @Nullable
    public static String getVpnUsername() {
        return Hawk.get(PREF_KEY_VPN_USERNAME);
    }

    public static void saveVpnUsername(@NonNull String username) {
        Hawk.put(PREF_KEY_VPN_USERNAME, username);
    }

    @Nullable
    public static String getVpnPassword() {
        return Hawk.get(PREF_KEY_VPN_PASSWORD);
    }

    public static void saveVpnPassword(@NonNull String password) {
        Hawk.put(PREF_KEY_VPN_PASSWORD, password);
    }

    public static void clearUser() {
        UserSettingPref user = new UserSettingPref();
        user.clear();
        Hawk.remove(PREF_KEY_COOKIE);
        Hawk.remove(PREF_KEY_SECRET);
        Hawk.remove(PREF_KEY_VPN_USERNAME);
        Hawk.remove(PREF_KEY_VPN_PASSWORD);
    }
}
