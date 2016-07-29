package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cypherpunk.android.vpn.utils.FontUtil;

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
        if (!isInEditMode()) {
            Typeface tf = FontUtil.get(context);
            setTypeface(tf);
        }
    }
}
