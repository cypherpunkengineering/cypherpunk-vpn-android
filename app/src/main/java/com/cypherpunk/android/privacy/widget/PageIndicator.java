package com.cypherpunk.android.privacy.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.cypherpunk.android.privacy.R;


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
        int pageCount = viewPager.getAdapter().getCount();
        for (int i = 0; i < pageCount; i++) {
            ImageView indicatorView = new ImageView(getContext());
            indicatorView.setImageDrawable(
                    ResourcesCompat.getDrawable(getResources(), R.drawable.indicator, null));
            addView(indicatorView);
        }
        setPagePosition(0);

        viewPager.addOnPageChangeListener(this);
    }

    public void setPagePosition(int position) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setSelected(false);
        }
        getChildAt(position).setSelected(true);
    }
}
