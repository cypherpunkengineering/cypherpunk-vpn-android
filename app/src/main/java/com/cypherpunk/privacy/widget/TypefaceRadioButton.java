package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.cypherpunk.privacy.utils.FontUtil;


public class TypefaceRadioButton extends RadioButton {

    public TypefaceRadioButton(Context context) {
        this(context, null);
    }

    public TypefaceRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypefaceRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            Typeface tf = FontUtil.getDosisMedium(context);
            setTypeface(tf);
        }
    }
}
