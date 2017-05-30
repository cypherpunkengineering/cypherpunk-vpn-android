package com.cypherpunk.privacy.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cypherpunk.privacy.datasource.account.Account;
import com.cypherpunk.privacy.datasource.account.Privacy;
import com.cypherpunk.privacy.datasource.account.Subscription;

/**
 * account settings
 */
public interface AccountSetting {

    boolean isSignedIn();

    // secret

    @Nullable
    String secret();

    void updateSecret(@Nullable String secret);

    // privacy

    @Nullable
    String username();

    @Nullable
    String password();

    void updatePrivacy(@NonNull Privacy privacy);

    // account

    @Nullable
    String accountId();

    @Nullable
    String email();

    void updateEmail(@Nullable String email);

    @NonNull
    Account.Type accountType();

    void updateAccount(@NonNull Account account);

    // subscription

    @NonNull
    Subscription subscription();

    void updateSubscription(@NonNull Subscription subscription);

    void clear();

}
