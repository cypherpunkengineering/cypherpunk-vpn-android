package com.cypherpunk.privacy.utils;

import android.content.Context;

public class ResourceUtil {

    public static int getFlagDrawableByKey(Context context, String key) {
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier("flag_" + key, "drawable", packageName);
    }
}
