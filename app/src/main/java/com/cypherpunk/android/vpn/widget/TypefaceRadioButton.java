package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.cypherpunk.android.vpn.utils.FontUtil;


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
            Typeface tf = FontUtil.getDosisRegular(context);
            setTypeface(tf);
        }
    }
}
