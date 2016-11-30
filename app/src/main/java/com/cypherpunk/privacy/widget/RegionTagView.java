package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.utils.FontUtil;


public class RegionTagView extends TextView {

    private static final String PREMIUM = "premium";
    private static final String DEVELOPER = "developer";

    public RegionTagView(Context context) {
        this(context, null);
    }

    public RegionTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RegionTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(FontUtil.getDosisSemiBold(context));
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
                    color = ContextCompat.getColor(getContext(), R.color.region_badge_premium_text);
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
