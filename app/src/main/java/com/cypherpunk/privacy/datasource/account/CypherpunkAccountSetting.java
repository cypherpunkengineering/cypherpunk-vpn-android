package com.cypherpunk.privacy.datasource.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.retrofit.adapter.ExpirationAdapter;
import com.cypherpunk.privacy.domain.repository.retrofit.interceptor.CookieStore;
import com.orhanobut.hawk.Hawk;

public class CypherpunkAccountSetting implements AccountSetting {

    private static final String PREF_NAME = "user_setting";

    private static final String KEY_ACCOUNT_MAIL = "mail";

    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_ACCOUNT_TYPE = "account_type";
    private static final String KEY_ACCOUNT_CONFIRMED = "confirmed";

    private static final String KEY_SUBSCRIPTION_TYPE = "status_renewal";
    private static final String KEY_SUBSCRIPTION_EXPIRATION = "status_expiration";
    private static final String KEY_SUBSCRIPTION_ACTIVE = "status_active";
    private static final String KEY_SUBSCRIPTION_RENEWS = "status_renews";

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

    @Override
    public boolean isPending() {
        final Account.Type type = accountType();
        return type == Account.Type.INVITATION || type == Account.Type.PENDING;
    }

    @Override
    public boolean isActive() {
        return subscription().isActive();
    }

    @Override
    public boolean isConfirmed() {
        return pref.getBoolean(KEY_ACCOUNT_CONFIRMED, false);
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
                .putBoolean(KEY_ACCOUNT_CONFIRMED, account.confirmed())
                .apply();
    }

    // subscription

    @NonNull
    @Override
    public Subscription subscription() {
        return new Subscription(
                Subscription.Type.find(pref.getString(KEY_SUBSCRIPTION_TYPE, "")),
                new ExpirationAdapter().fromJson(pref.getString(KEY_SUBSCRIPTION_EXPIRATION, "")),
                pref.getBoolean(KEY_SUBSCRIPTION_ACTIVE, false),
                pref.getBoolean(KEY_SUBSCRIPTION_RENEWS, false));
    }

    @Override
    public void updateSubscription(@NonNull Subscription subscription) {
        pref.edit()
                .putString(KEY_SUBSCRIPTION_TYPE, subscription.type().value())
                .putString(KEY_SUBSCRIPTION_EXPIRATION, new ExpirationAdapter().toJson(subscription.expiration()))
                .putBoolean(KEY_SUBSCRIPTION_ACTIVE, subscription.isActive())
                .putBoolean(KEY_SUBSCRIPTION_RENEWS, subscription.isRenews())
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
