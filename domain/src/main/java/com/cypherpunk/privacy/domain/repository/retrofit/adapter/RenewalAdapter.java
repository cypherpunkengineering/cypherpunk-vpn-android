package com.cypherpunk.privacy.domain.repository.retrofit.adapter;

import com.cypherpunk.privacy.domain.model.Subscription;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

/**
 * adapter from String to Renewal
 */
public class RenewalAdapter {
    @ToJson
    String toJson(Subscription.Renewal renewal) {
        if (renewal == null) {
            return Subscription.Renewal.NONE.value();
        }
        return renewal.value();
    }

    @FromJson
    Subscription.Renewal fromJson(String renewal) {
        return Subscription.Renewal.find(renewal);
    }
}
