package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Dosis-Regular font TextView
 */
public class TypefaceTextView extends TextView {

    public TypefaceTextView(Context context) {
        this(context, null);
    }

    public TypefaceTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Dosis-Regular.otf");
        setTypeface(tf);
    }
}
