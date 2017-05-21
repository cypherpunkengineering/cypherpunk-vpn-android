package com.cypherpunk.privacy.ui.common;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;


public class FontCache {

    private static Typeface dosisRegular;
    private static Typeface dosisMedium;
    private static Typeface dosisSemiBold;
    private static Typeface dosisBold;
    private static Typeface inconsolataRegular;

    @NonNull
    public static Typeface getDosisRegular(Context context) {
        if (dosisRegular == null) {
            dosisRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Regular.otf");
        }
        return dosisRegular;
    }

    @NonNull
    public static Typeface getDosisSemiBold(Context context) {
        if (dosisSemiBold == null) {
            dosisSemiBold = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-SemiBold.otf");
        }
        return dosisSemiBold;
    }

    @NonNull
    public static Typeface getDosisBold(Context context) {
        if (dosisBold == null) {
            dosisBold = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Bold.otf");
        }
        return dosisBold;
    }

    @NonNull
    public static Typeface getDosisMedium(Context context) {
        if (dosisMedium == null) {
            dosisMedium = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Medium.otf");
        }
        return dosisMedium;
    }

    @NonNull
    public static Typeface getInconsolataRegular(Context context) {
        if (inconsolataRegular == null) {
            inconsolataRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Inconsolata-Regular.ttf");
        }
        return inconsolataRegular;
    }
}
