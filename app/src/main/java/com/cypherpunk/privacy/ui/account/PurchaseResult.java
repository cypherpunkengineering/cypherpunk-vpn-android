package com.cypherpunk.privacy.ui.account;

public class PurchaseResult {

    private final boolean isSuccess;
    private final String sku;
    private final String resultJson;
    private final String log;

    public PurchaseResult(boolean isSuccess, String sku, String resultJson, String log) {
        this.isSuccess = isSuccess;
        this.sku = sku;
        this.resultJson = resultJson;
        this.log = log;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getSku() {
        return sku;
    }

    public String getResultJson() {
        return resultJson;
    }

    public String getLog() {
        return log;
    }
}
