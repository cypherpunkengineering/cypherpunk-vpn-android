package com.cypherpunk.privacy.utils;

import android.content.Context;
import android.graphics.Typeface;


public class FontUtil {

    private static Typeface dosisRegular;
    private static Typeface dosisMedium;
    private static Typeface dosisSemiBold;
    private static Typeface dosisBold;
    private static Typeface inconsolataRegular;

    public static Typeface getDosisRegular(Context context) {
        if (dosisRegular == null) {
            dosisRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Regular.otf");
        }
        return dosisRegular;
    }

    public static Typeface getDosisSemiBold(Context context) {
        if (dosisSemiBold == null) {
            dosisSemiBold = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-SemiBold.otf");
        }
        return dosisSemiBold;
    }

    public static Typeface getDosisBold(Context context) {
        if (dosisBold == null) {
            dosisBold = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Bold.otf");
        }
        return dosisBold;
    }

    public static Typeface getDosisMedium(Context context) {
        if (dosisMedium == null) {
            dosisMedium = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Medium.otf");
        }
        return dosisMedium;
    }

    public static Typeface getInconsolataRegular(Context context) {
        if (inconsolataRegular == null) {
            inconsolataRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Inconsolata-Regular.ttf");
        }
        return inconsolataRegular;
    }
}
