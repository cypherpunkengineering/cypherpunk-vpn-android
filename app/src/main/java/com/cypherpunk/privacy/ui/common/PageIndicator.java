package com.cypherpunk.privacy.ui.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.cypherpunk.privacy.R;

public class PageIndicator extends RadioGroup implements ViewPager.OnPageChangeListener {

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setPagePosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void setViewPager(@NonNull ViewPager viewPager) {
        removeAllViews();
        final int pageCount = viewPager.getAdapter().getCount();
        for (int i = 0; i < pageCount; i++) {
            final ImageView iv = new ImageView(getContext());
            iv.setImageResource(R.drawable.indicator);
            addView(iv);
        }

        setPagePosition(0);

        viewPager.addOnPageChangeListener(this);
    }

    public void setPagePosition(int position) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setSelected(i == position);
        }
    }
}
