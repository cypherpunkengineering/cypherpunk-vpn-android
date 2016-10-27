package com.cypherpunk.privacy.data.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.model.UserSettingPref;
import com.orhanobut.hawk.Hawk;

public class UserManager {

    private static final String PREF_KEY_PASSWORD = "password";
    private static final String PREF_KEY_SECRET = "secret";


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
    public static String getPassword() {
        return Hawk.get(PREF_KEY_PASSWORD);
    }

    public static void savePassword(@NonNull String password) {
        Hawk.put(PREF_KEY_PASSWORD, password);
    }

    @Nullable
    public static String getSecret() {
        return Hawk.get(PREF_KEY_SECRET);
    }

    public static void saveSecret(@NonNull String secret) {
        Hawk.put(PREF_KEY_SECRET, secret);
    }

    public static void clearUser() {
        UserSettingPref user = new UserSettingPref();
        user.clear();
        Hawk.remove(PREF_KEY_PASSWORD);
        Hawk.remove(PREF_KEY_SECRET);
    }
}