package com.cypherpunk.privacy.widget;


import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

import com.cypherpunk.privacy.R;

public class StarView extends AppCompatImageView implements Checkable, View.OnClickListener {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private boolean checked;
    private Listener listener;

    public interface Listener {
        void onCheckedChanged(boolean checked);
    }

    public StarView(Context context) {
        this(context, null);
    }

    public StarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setOnClickListener(this);
        setImageResource(R.drawable.star);
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked == checked) {
            return;
        }
        this.checked = checked;
        listener.onCheckedChanged(checked);
        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }


    @Override
    public void toggle() {
        setChecked(!checked);
    }


    @Override
    public void onClick(View view) {
        toggle();
    }

    @Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    public void setOnCheckedChangeListener(Listener listener) {
        this.listener = listener;
    }
}
