package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cypherpunk.privacy.R;


public class RegionTagView extends TextView {

    public static final String PREMIUM = "premium";
    public static final String DEVELOPER = "developer";
//    public static final String STAFF = "staff";

    public RegionTagView(Context context) {
        this(context, null);
    }

    public RegionTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RegionTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(null, Typeface.BOLD);
    }


    public void setRegionLevel(String level) {
        setVisibility(level.equals(PREMIUM) || level.equals(DEVELOPER) ? VISIBLE : INVISIBLE);
        if (level.equals(PREMIUM) || level.equals(DEVELOPER)) {
            @StringRes int text = 0;
            @DrawableRes int background = 0;
            @ColorInt int color = 0;
            switch (level) {
                case PREMIUM:
                    background = R.drawable.region_badge_premium;
                    text = R.string.region_level_badge_premium;
                    color = Color.BLACK;
                    break;
                case DEVELOPER:
                    background = R.drawable.region_badge_dev;
                    text = R.string.region_level_badge_dev;
                    color = Color.WHITE;
                    break;
            }
            setText(text);
            setBackgroundResource(background);
            setTextColor(color);
        }
    }
}
