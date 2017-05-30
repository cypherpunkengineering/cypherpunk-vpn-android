package com.cypherpunk.privacy.domain.repository.retrofit.result;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.datasource.account.Account;
import com.cypherpunk.privacy.datasource.account.Privacy;
import com.cypherpunk.privacy.datasource.account.Subscription;

public class StatusResult {

    public final String secret;
    public final Privacy privacy;
    public final Account account;
    public final Subscription subscription;

    public StatusResult(@NonNull String secret, @NonNull Privacy privacy,
                        @NonNull Account account, @NonNull Subscription subscription) {
        this.secret = secret;
        this.privacy = privacy;
        this.account = account;
        this.subscription = subscription;
    }
}
