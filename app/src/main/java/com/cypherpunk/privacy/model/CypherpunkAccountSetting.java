package com.cypherpunk.privacy.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.account.Account;
import com.cypherpunk.privacy.domain.model.account.Privacy;
import com.cypherpunk.privacy.domain.model.account.Subscription;
import com.cypherpunk.privacy.domain.repository.retrofit.adapter.ExpirationAdapter;
import com.cypherpunk.privacy.domain.repository.retrofit.interceptor.CookieStore;
import com.orhanobut.hawk.Hawk;

public class CypherpunkAccountSetting implements AccountSetting {

    private static final String PREF_NAME = "user_setting";

    private static final String KEY_ACCOUNT_MAIL = "mail";

    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_ACCOUNT_TYPE = "account_type";
    private static final String KEY_STATUS_RENEWAL = "status_renewal";
    private static final String KEY_STATUS_EXPIRATION = "status_expiration";

    private static final String PREF_KEY_VPN_USERNAME = "vpn_username";
    private static final String PREF_KEY_VPN_PASSWORD = "vpn_password";

    private static final String PREF_KEY_SECRET = "secret";

    private final SharedPreferences pref;

    public CypherpunkAccountSetting(@NonNull Context c) {
        pref = c.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isSignedIn() {
        return !TextUtils.isEmpty(email());
    }

    // secret

    @Nullable
    @Override
    public String secret() {
        return Hawk.get(PREF_KEY_SECRET);
    }

    @Override
    public void updateSecret(@Nullable String secret) {
        Hawk.put(PREF_KEY_SECRET, secret);
    }

    // privacy

    @Nullable
    @Override
    public String username() {
        return Hawk.get(PREF_KEY_VPN_USERNAME);
    }

    @Nullable
    @Override
    public String password() {
        return Hawk.get(PREF_KEY_VPN_PASSWORD);
    }

    @Override
    public void updatePrivacy(@NonNull Privacy privacy) {
        Hawk.put(PREF_KEY_VPN_USERNAME, privacy.username());
        Hawk.put(PREF_KEY_VPN_PASSWORD, privacy.password());
    }

    // account

    @Nullable
    @Override
    public String accountId() {
        return pref.getString(KEY_ACCOUNT_ID, null);
    }

    @Nullable
    @Override
    public String email() {
        return pref.getString(KEY_ACCOUNT_MAIL, null);
    }

    public void updateEmail(@Nullable String email) {
        pref.edit().putString(KEY_ACCOUNT_MAIL, email).apply();
    }

    @NonNull
    @Override
    public Account.Type accountType() {
        return Account.Type.find(pref.getString(KEY_ACCOUNT_TYPE, null));
    }

    public void updateAccount(@NonNull Account account) {
        pref.edit()
                .putString(KEY_ACCOUNT_ID, account.id())
                .putString(KEY_ACCOUNT_TYPE, account.type().value())
                .apply();
    }

    // subscription

    @NonNull
    @Override
    public Subscription subscription() {
        return new Subscription(Subscription.Renewal.find(pref.getString(KEY_STATUS_RENEWAL, "")),
                new ExpirationAdapter().fromJson(pref.getString(KEY_STATUS_EXPIRATION, "")));
    }

    @Override
    public void updateSubscription(@NonNull Subscription subscription) {
        pref.edit()
                .putString(KEY_STATUS_RENEWAL, subscription.renewal().value())
                .putString(KEY_STATUS_EXPIRATION, new ExpirationAdapter().toJson(subscription.expiration()))
                .apply();
    }

    //

    @Override
    public void clear() {
        pref.edit().clear().apply();

        Hawk.remove(PREF_KEY_VPN_USERNAME);
        Hawk.remove(PREF_KEY_VPN_PASSWORD);
        Hawk.remove(PREF_KEY_SECRET);
        CookieStore.clear();
    }
}
