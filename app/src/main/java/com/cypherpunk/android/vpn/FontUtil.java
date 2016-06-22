package com.cypherpunk.android.vpn;

import android.content.Context;
import android.graphics.Typeface;


public class FontUtil {

    private static Typeface sTypeface;

    public static Typeface get(Context context) {
        if (sTypeface == null) {
            sTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Regular.otf");
        }
        return sTypeface;
    }
}
