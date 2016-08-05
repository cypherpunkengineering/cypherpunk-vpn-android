package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.widget.Button;

import com.cypherpunk.android.vpn.utils.FontUtil;


public class TypefaceButton extends Button {

    public TypefaceButton(Context context) {
        this(context, null);
    }

    public TypefaceButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypefaceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            Typeface tf = FontUtil.getDosisRegular(context);
            setTypeface(tf);
        }
    }
}