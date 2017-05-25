package com.cypherpunk.privacy.ui.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.widget.TypefaceTextView;

/**
 * TODO: deprecate when use font resource
 */
public class CustomToolbar extends Toolbar {

    private final TypefaceTextView titleView;

    public CustomToolbar(Context context) {
        this(context, null);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.toolbarStyle);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.custom_toolbar, this);
        titleView = (TypefaceTextView) findViewById(R.id.title);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(null);
        titleView.setText(title);
    }
}
