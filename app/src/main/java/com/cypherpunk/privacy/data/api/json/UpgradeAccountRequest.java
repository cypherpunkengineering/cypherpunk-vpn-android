package com.cypherpunk.privacy.data.api.json;


public class UpgradeAccountRequest {

    private final String accountId;

    private final String planId;

    private final String transactionDataObject;

    public UpgradeAccountRequest(String accountId, String planId, String transactionDataObject) {
        this.accountId = accountId;
        this.planId = planId;
        this.transactionDataObject = transactionDataObject;
    }
}
