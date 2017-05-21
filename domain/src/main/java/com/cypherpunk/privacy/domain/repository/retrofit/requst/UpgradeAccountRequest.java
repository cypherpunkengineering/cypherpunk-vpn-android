package com.cypherpunk.privacy.domain.repository.retrofit.requst;

import android.support.annotation.NonNull;

public class UpgradeAccountRequest {

    @NonNull
    private final String accountId;

    @NonNull
    private final String planId;

    @NonNull
    private final String transactionDataObject;

    public UpgradeAccountRequest(@NonNull String accountId, @NonNull String planId,
                                 @NonNull String transactionDataObject) {
        this.accountId = accountId;
        this.planId = planId;
        this.transactionDataObject = transactionDataObject;
    }
}
