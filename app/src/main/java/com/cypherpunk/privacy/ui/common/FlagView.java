package com.cypherpunk.privacy.ui.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * flag for country
 */
public class FlagView extends AppCompatImageView {

    public FlagView(Context context) {
        super(context);
    }

    public FlagView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FlagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCountry(String countryCode) {
        final String name = "flag_" + countryCode.toLowerCase();
        final String packageName = getContext().getPackageName();
        final int resId = getResources().getIdentifier(name, "drawable", packageName);
        setImageResource(resId);
    }
}
