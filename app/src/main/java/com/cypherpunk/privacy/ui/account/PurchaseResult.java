package com.cypherpunk.privacy.ui.account;

import android.support.annotation.Nullable;

class PurchaseResult {

    private final boolean isSuccess;
    @Nullable
    private final String sku;
    @Nullable
    private final String resultJson;
    private final String log;

    PurchaseResult(boolean isSuccess, @Nullable String sku, @Nullable String resultJson, String log) {
        this.isSuccess = isSuccess;
        this.sku = sku;
        this.resultJson = resultJson;
        this.log = log;
    }

    boolean isSuccess() {
        return isSuccess;
    }

    @Nullable
    String getSku() {
        return sku;
    }

    @Nullable
    String getResultJson() {
        return resultJson;
    }

    String getLog() {
        return log;
    }
}
