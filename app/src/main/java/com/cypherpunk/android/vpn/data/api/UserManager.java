package com.cypherpunk.android.vpn.data.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orhanobut.hawk.Hawk;

public class UserManager {

    private static final String PREF_KEY_MAIL = "mail";
    private static final String PREF_KEY_PASSWORD = "password";

    private static UserManager singleton = null;

    @NonNull
    public static synchronized UserManager getInstance(@NonNull Context context) {
        if (singleton == null) {
            singleton = new UserManager(context);
        }
        return singleton;
    }

    @NonNull
    private final Context context;

    private UserManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean isSignedIn() {
        String mail = getMailAddress();
        return mail != null && mail.length() > 0;
    }

    @Nullable
    public String getMailAddress() {
        return getPrefs(context).getString(PREF_KEY_MAIL, null);
    }

    public void saveMailAddress(@NonNull String mail) {
        getPrefs(context).edit().putString(PREF_KEY_MAIL, mail).apply();
    }

    @Nullable
    public String getPassword() {
        return Hawk.get(PREF_KEY_PASSWORD);
    }

    public void savePassword(@NonNull String password) {
        Hawk.put(PREF_KEY_PASSWORD, password);
    }

    @NonNull
    private SharedPreferences getPrefs(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
