package com.cypherpunk.android.vpn.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cypherpunk.android.vpn.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;


public class ConnectionStatusView extends LinearLayout {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionStatus {
    }

    private final TextView statusText;
    private final TextView[] textViews = new TextView[3];
    AnimatorSet animatorSet = new AnimatorSet();

    public ConnectionStatusView(Context context) {
        this(context, null);
    }

    public ConnectionStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectionStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ConnectionStatusView);
        float textSize = a.getDimension(R.styleable.ConnectionStatusView_textSize,
                getResources().getDimension(R.dimen.connection_status_text_size));
        a.recycle();

        statusText = new TypefaceTextView(context);
        statusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        addView(statusText);
        ArrayList<Animator> animators = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            TypefaceTextView textView = new TypefaceTextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            textView.setText(".");
            textView.setAlpha(0f);
            textView.setVisibility(GONE);

            ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
            animator.setDuration(300);
            animator.setStartDelay(1 > i ? 300 * i : 300);
            animators.add(animator);

            addView(textView);
            textViews[i] = textView;
        }
        animatorSet.playSequentially(animators);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                for (TextView textView : textViews) {
                    textView.setAlpha(0f);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        setStatus(DISCONNECTED);
    }

    public void setStatus(@ConnectionStatus int status) {
        for (TextView textView : textViews) {
            textView.setVisibility(status == CONNECTING ? VISIBLE : GONE);
        }
        switch (status) {
            case DISCONNECTED:
                animatorSet.cancel();
                statusText.setText(R.string.status_disconnected);
                break;
            case CONNECTING:
                statusText.setText(R.string.status_connecting);
                animatorSet.start();
                break;
            case CONNECTED:
                animatorSet.cancel();
                statusText.setText(R.string.status_connected);
                break;
        }
    }
}
