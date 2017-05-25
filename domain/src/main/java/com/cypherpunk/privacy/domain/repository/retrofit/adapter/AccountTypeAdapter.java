package com.cypherpunk.privacy.domain.repository.retrofit.adapter;

import com.cypherpunk.privacy.domain.model.account.Account;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

/**
 * adapter from String to Renewal
 */
public class AccountTypeAdapter {
    @ToJson
    String toJson(Account.Type type) {
        if (type == null) {
            return Account.Type.FREE.value();
        }
        return type.value();
    }

    @FromJson
    Account.Type fromJson(String type) {
        return Account.Type.find(type);
    }
}
