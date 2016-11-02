package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class UnswipeablePager extends ViewPager {

    public UnswipeablePager(Context context) {
        super(context);
    }

    public UnswipeablePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
