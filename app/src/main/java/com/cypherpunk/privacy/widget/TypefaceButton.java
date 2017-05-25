package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.cypherpunk.privacy.ui.common.FontCache;

/**
 * Dosis font Button
 */
public class TypefaceButton extends AppCompatButton {

    public TypefaceButton(Context context) {
        this(context, null);
    }

    public TypefaceButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.buttonStyle);
    }

    public TypefaceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            Typeface tf = FontCache.getDosisBold(context);
            setTypeface(tf);
        }
    }
}
