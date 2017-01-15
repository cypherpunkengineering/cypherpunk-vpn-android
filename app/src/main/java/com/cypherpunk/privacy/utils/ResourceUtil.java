package com.cypherpunk.privacy.utils;

import android.content.Context;

import static com.os.operando.garum.utils.Cache.getContext;

public class ResourceUtil {

    public static int getFlagDrawableByKey(Context context, String key) {
        String packageName = getContext().getPackageName();
        return context.getResources().getIdentifier("flag_" + key, "drawable", packageName);
    }

    public static String getStringByKey(Context context, String key) {
        String packageName = getContext().getPackageName();
        int id = context.getResources().getIdentifier(key, "string", packageName);
        String str;
        try {
            str = context.getResources().getString(id);
        } catch (Exception NotFoundException) {
            str = key;
        }
        return str;
    }
}
