package com.cypherpunk.privacy.domain.repository.retrofit.adapter;

import android.text.TextUtils;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * adapter from String to Renewal
 */
public class ExpirationAdapter {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @ToJson
    public String toJson(Date expiration) {
        if (expiration == null) {
            return null;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault());
        return sdf.format(expiration);
    }

    @FromJson
    public Date fromJson(String expirationText) {
        if (!TextUtils.isEmpty(expirationText)) {
            final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault());
            try {
                return sdf.parse(expirationText);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
