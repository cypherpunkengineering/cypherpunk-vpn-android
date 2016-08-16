package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ViewPlanBinding;


public class PlanView extends RelativeLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private ViewPlanBinding binding;
    private boolean checked;

    public PlanView(Context context) {
        this(context, null);
    }

    public PlanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_plan, this, true);
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            refreshDrawableState();
        }
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
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    public void setPlan(@NonNull String planName, @NonNull String description, @NonNull String price) {
        binding.plan.setText(planName);
        binding.planDescription.setText(description);
        binding.price.setText(price);
    }
}
