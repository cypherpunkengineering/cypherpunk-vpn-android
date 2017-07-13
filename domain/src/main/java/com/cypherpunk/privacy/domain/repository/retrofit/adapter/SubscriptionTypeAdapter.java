package com.cypherpunk.privacy.domain.repository.retrofit.adapter;

import com.cypherpunk.privacy.datasource.account.Subscription;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

/**
 * adapter from String to Renewal
 */
public class SubscriptionTypeAdapter {
    @ToJson
    String toJson(Subscription.Type type) {
        if (type == null) {
            return Subscription.Type.NONE.value();
        }
        return type.value();
    }

    @FromJson
    Subscription.Type fromJson(String renewal) {
        return Subscription.Type.find(renewal);
    }
}
