package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cypherpunk.privacy.R;

import butterknife.BindView;
import butterknife.ButterKnife;

// TODO: refactor
public class PlanView extends FrameLayout {

    @BindView(R.id.card)
    RelativeLayout cardView;

    @BindView(R.id.plan)
    TextView planView;

    @BindView(R.id.price)
    TextView priceView;

    @BindView(R.id.badge)
    TextView badgeView;

    @BindView(R.id.current_plan)
    TextView currentPlanView;

    public PlanView(Context context) {
        this(context, null);
    }

    public PlanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_plan, this);
        ButterKnife.bind(this);
    }

    /**
     * @param planName     plan name
     * @param price        price
     * @param badgeVisible best value badge visibility
     */
    public void setPlan(@NonNull String planName, @NonNull String price, boolean badgeVisible) {
        planView.setText(planName);
        priceView.setText(price);
        badgeView.setVisibility(badgeVisible ? VISIBLE : INVISIBLE);
        cardView.setBackgroundResource(badgeVisible ? R.drawable.plan_background_badge : R.drawable.plan_background);
        priceView.setTextColor(ContextCompat.getColor(getContext(), badgeVisible ? R.color.plan_badge_text : R.color.plan_price));
        planView.setTextColor(ContextCompat.getColor(getContext(), badgeVisible ? R.color.plan_badge_text : R.color.plan_text));
    }

    public void setCurrentPlan() {
        cardView.setAlpha(0.15f);
        currentPlanView.setVisibility(VISIBLE);
        badgeView.setVisibility(INVISIBLE);
        setClickable(false);
    }
}
