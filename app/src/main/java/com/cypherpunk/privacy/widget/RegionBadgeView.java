package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.vpn.Level;
import com.cypherpunk.privacy.ui.common.FontCache;

public class RegionBadgeView extends AppCompatTextView {

    public RegionBadgeView(Context context) {
        super(context);
    }

    public RegionBadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RegionBadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLevel(@NonNull Level level) {
        switch (level) {
            case PREMIUM:
                setText(R.string.region_level_badge_premium);
                setBackgroundResource(R.drawable.region_badge_premium);
                setTextColor(ContextCompat.getColor(getContext(), R.color.region_badge_premium_text));
                setTypeface(FontCache.getDosisSemiBold(getContext()));
                break;

            case DEVELOPER:
                setText(R.string.region_level_badge_dev);
                setBackgroundResource(R.drawable.region_badge_developer);
                setTextColor(Color.WHITE);
                setTypeface(FontCache.getDosisSemiBold(getContext()));
                break;

            case UNAVAILABLE:
                setText(R.string.region_level_badge_unavailable);
                setBackgroundResource(0);
                setTextColor(ContextCompat.getColor(getContext(), R.color.region_disabled_text));
                setTypeface(FontCache.getDosisRegular(getContext()));
                break;
        }
    }
}
