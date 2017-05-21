package com.cypherpunk.privacy.utils;

import android.content.Context;
import android.support.annotation.DrawableRes;

public class ResourceUtil {

    @DrawableRes
    public static int getFlagDrawableByKey(Context context, String key) {
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier("flag_" + key, "drawable", packageName);
    }
}
