package com.cypherpunk.privacy.domain.repository.retrofit.adapter;

import android.text.TextUtils;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * adapter from String to Renewal
 */
public class ExpirationAdapter {

    private static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @ToJson
    public String toJson(Date expiration) {
        if (expiration == null) {
            return null;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT, Locale.ENGLISH);
        return sdf.format(expiration);
    }

    @FromJson
    public Date fromJson(String expirationText) {
        if (!TextUtils.isEmpty(expirationText)) {
            final SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT, Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                return sdf.parse(expirationText);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
