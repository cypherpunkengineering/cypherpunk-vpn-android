package com.cypherpunk.privacy.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.domain.model.Account;
import com.cypherpunk.privacy.domain.model.Subscription;
import com.cypherpunk.privacy.domain.repository.retrofit.interceptor.CookieStore;
import com.cypherpunk.privacy.domain.repository.retrofit.adapter.ExpirationAdapter;
import com.orhanobut.hawk.Hawk;

import java.util.Date;

public class UserSetting {

    private static final String PREF_NAME = "user_setting";

    private static final String KEY_MAIL = "mail";

    private static final String KEY_STATUS_TYPE = "status_type";
    private static final String KEY_STATUS_RENEWAL = "status_renewal";
    private static final String KEY_STATUS_EXPIRATION = "status_expiration";

    private static final String PREF_KEY_VPN_USERNAME = "vpn_username";
    private static final String PREF_KEY_VPN_PASSWORD = "vpn_password";

    private static final String PREF_KEY_SECRET = "secret";

    private final SharedPreferences pref;

    private UserSetting(@NonNull Context c) {
        pref = c.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSignedIn() {
        return !TextUtils.isEmpty(mail());
    }

    @Nullable
    public String mail() {
        return pref.getString(KEY_MAIL, null);
    }

    public void updateMail(@NonNull String mail) {
        pref.edit().putString(KEY_MAIL, mail).apply();
    }

    //

    public Account.Type accountType() {
        return Account.Type.find(pref.getString(KEY_STATUS_TYPE, null));
    }

    public void updateAccountType(@NonNull Account.Type type) {
        pref.edit().putString(KEY_STATUS_TYPE, type.value()).apply();
    }

    @NonNull
    public Subscription subscriptionPlan() {
        return new Subscription(
                Subscription.Renewal.find(pref.getString(KEY_STATUS_RENEWAL, "")),
                new ExpirationAdapter().fromJson(pref.getString(KEY_STATUS_EXPIRATION, "")));
    }

    public void updateStatus(@NonNull Account.Type type, @NonNull Subscription.Renewal renewal, @Nullable Date expiration) {
        pref.edit()
                .putString(KEY_STATUS_TYPE, type.value())
                .putString(KEY_STATUS_RENEWAL, renewal.value())
                .putString(KEY_STATUS_EXPIRATION, new ExpirationAdapter().toJson(expiration))
                .apply();
    }

    //

    @Nullable
    public String vpnUsername() {
        return Hawk.get(PREF_KEY_VPN_USERNAME);
    }

    @Nullable
    public String vpnPassword() {
        return Hawk.get(PREF_KEY_VPN_PASSWORD);
    }

    public void updateVpnUserNameAndPassword(@Nullable String username, @Nullable String password) {
        Hawk.put(PREF_KEY_VPN_USERNAME, username);
        Hawk.put(PREF_KEY_VPN_PASSWORD, password);
    }

    //

    @Nullable
    public String secret() {
        return Hawk.get(PREF_KEY_SECRET);
    }

    public void updateSecret(@Nullable String secret) {
        Hawk.put(PREF_KEY_SECRET, secret);
    }

    //

    public void clear() {
        pref.edit().clear().apply();

        Hawk.remove(PREF_KEY_VPN_USERNAME);
        Hawk.remove(PREF_KEY_VPN_PASSWORD);
        Hawk.remove(PREF_KEY_SECRET);
        CookieStore.clear();
    }

    //
    //
    //

    private static UserSetting instance;

    public static void init(@NonNull Context context) {
        instance = new UserSetting(context);
    }

    @NonNull
    public static UserSetting instance() {
        if (instance == null) {
            throw new IllegalStateException("call init() first");
        }
        return instance;
    }
}
