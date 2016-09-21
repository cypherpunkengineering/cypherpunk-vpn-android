package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;


public class TimeTexView extends TypefaceTextView {

    private long base;

    public TimeTexView(Context context) {
        this(context, null);
    }

    public TimeTexView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeTexView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBaseTime(System.currentTimeMillis());
        mTickRunnable.run();
    }

    public void setBaseTime(long time) {
        base = time;
    }

    private void updateText(long now) {
        long seconds = now - base;
        seconds /= 1000;
        String text = DateUtils.formatElapsedTime(new StringBuilder(8), seconds);
        setText(text);
    }

    private final Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            updateText(System.currentTimeMillis());
            postDelayed(mTickRunnable, 1000);
        }
    };
}
