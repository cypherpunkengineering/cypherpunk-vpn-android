package com.cypherpunk.privacy.ui.region;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.Level;
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

    public void setLevel(@NonNull Level level, boolean isAvailable) {
        if (isAvailable) {
            switch (level) {
                case PREMIUM:
                    setText(R.string.region_level_badge_premium);
                    setBackgroundResource(R.drawable.region_badge_premium);
                    setTextColor(ContextCompat.getColor(getContext(), R.color.text_region_badge_premium));
                    setTypeface(FontCache.getDosisBold(getContext()));
                    break;

                case DEVELOPER:
                    setText(R.string.region_level_badge_dev);
                    setBackgroundResource(R.drawable.region_badge_developer);
                    setTextColor(ContextCompat.getColor(getContext(), R.color.text_region_badge_developer));
                    setTypeface(FontCache.getDosisBold(getContext()));
                    break;

                case FREE:
                    setText(null);
                    setBackgroundResource(0);
                    setTextColor(ContextCompat.getColor(getContext(), R.color.white70));
                    setTypeface(FontCache.getDosisRegular(getContext()));
                    break;
            }
        } else {
            setText(R.string.region_level_badge_unavailable);
            setBackgroundResource(0);
            setTextColor(ContextCompat.getColor(getContext(), R.color.white70));
            setTypeface(FontCache.getDosisRegular(getContext()));
        }
    }
}
