package com.cypherpunk.privacy.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.ui.common.FontCache;

/**
 * Dosis font TextView
 */
public class TypefaceTextView extends AppCompatTextView {

    public static final int DOSIS_REGULAR = 0;
    public static final int DOSIS_MEDIUM = 1;
    public static final int DOSIS_SEMI_BOLD = 2;
    public static final int DOSIS_BOLD = 3;
    public static final int INCONSOLATE_REGULAR = 4;

    public TypefaceTextView(Context context) {
        this(context, null);
    }

    public TypefaceTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
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
                tf = FontCache.getDosisRegular(getContext());
                break;
            case DOSIS_MEDIUM:
                tf = FontCache.getDosisMedium(getContext());
                break;
            case DOSIS_SEMI_BOLD:
                tf = FontCache.getDosisSemiBold(getContext());
                break;
            case DOSIS_BOLD:
                tf = FontCache.getDosisBold(getContext());
                break;
            case INCONSOLATE_REGULAR:
                tf = FontCache.getInconsolataRegular(getContext());
                break;
        }
        if (tf != null) {
            setTypeface(tf);
        }
    }
}
