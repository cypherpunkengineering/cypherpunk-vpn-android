package com.cypherpunk.privacy.ui.startup;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * swipe impossible
 */
public class TutorialPager extends ViewPager {

    public TutorialPager(Context context) {
        super(context);
    }

    public TutorialPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
