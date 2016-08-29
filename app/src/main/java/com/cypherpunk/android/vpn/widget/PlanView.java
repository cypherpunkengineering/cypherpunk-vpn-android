package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ViewPlanBinding;


public class PlanView extends RelativeLayout {

    private ViewPlanBinding binding;

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
        setClickable(true);
    }

    /**
     * @param planName     plan name
     * @param price        price
     * @param badgeVisible best value badge visibility
     */
    public void setPlan(@NonNull String planName, @NonNull String price, boolean badgeVisible) {
        binding.plan.setText(planName);
        binding.price.setText(price);
        binding.badge.setVisibility(badgeVisible ? VISIBLE : INVISIBLE);
    }
}
