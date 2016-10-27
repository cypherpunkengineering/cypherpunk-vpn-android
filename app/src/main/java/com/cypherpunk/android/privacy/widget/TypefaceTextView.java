package com.cypherpunk.android.privacy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cypherpunk.android.privacy.R;
import com.cypherpunk.android.privacy.utils.FontUtil;

/**
 * Dosis font TextView
 */
public class TypefaceTextView extends TextView {

    public static final int DOSIS_REGULAR = 0;
    public static final int DOSIS_MEDIUM = 1;
    public static final int DOSIS_SEMI_BOLD = 2;
    public static final int DOSIS_BOLD = 3;
    public static final int INCONSOLATE_REGULAR = 4;

    public TypefaceTextView(Context context) {
        this(context, null);
    }

    public TypefaceTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int typeIndex = 0;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypefaceTextView);
        assert a != null;
        try {
            typeIndex = a.getInt(R.styleable.TypefaceTextView_fontName, 0);
        } finally {
            a.recycle();
        }

        if (!isInEditMode()) {
            setTypefaceDosis(typeIndex);
        }
    }

    public void setTypefaceDosis(int typeIndex) {
        if (isInEditMode()) {
            return;
        }

        Typeface tf = null;
        switch (typeIndex) {
            case DOSIS_REGULAR:
                tf = FontUtil.getDosisRegular(getContext());
                break;
            case DOSIS_MEDIUM:
                tf = FontUtil.getDosisMedium(getContext());
                break;
            case DOSIS_SEMI_BOLD:
                tf = FontUtil.getDosisSemiBold(getContext());
                break;
            case DOSIS_BOLD:
                tf = FontUtil.getDosisBold(getContext());
                break;
            case INCONSOLATE_REGULAR:
                tf = FontUtil.getInconsolataRegular(getContext());
                break;
        }
        if (tf != null) {
            setTypeface(tf);
        }
    }
}
