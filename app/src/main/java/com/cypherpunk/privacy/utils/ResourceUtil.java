package com.cypherpunk.privacy.utils;

import android.content.Context;
import android.support.annotation.DrawableRes;

public class ResourceUtil {

    @DrawableRes
    public static int getFlag(Context context, String key) {
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier("flag_" + key.toLowerCase(), "drawable", packageName);
    }
}
