package com.cypherpunk.privacy.ui.common;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cypherpunk.privacy.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class VpnFlatButton extends FrameLayout {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    private CompoundButton.OnCheckedChangeListener listener;

    private final VpnFlatButton.VpnCoreButton buttonView;
    private final ObjectAnimator anim;
    private final ImageView imageView;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionStatus {
    }

    public VpnFlatButton(Context context) {
        this(context, null);
    }

    public VpnFlatButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VpnFlatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setClickable(true);

        imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.connecting);
        addView(imageView, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

        anim = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(3000);
        anim.setRepeatCount(ValueAnimator.INFINITE);

        buttonView = new VpnFlatButton.VpnCoreButton(context);
        addView(buttonView);
        buttonView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });

        setStatus(DISCONNECTED);
    }

    public void setStatus(@ConnectionStatus int status) {
        buttonView.setStatus(status);
        switch (status) {
            case DISCONNECTED:
            case CONNECTED:
                anim.end();
                imageView.setVisibility(GONE);
                break;
            case CONNECTING:
                imageView.setVisibility(VISIBLE);
                anim.start();
                break;
        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public static class VpnCoreButton extends CompoundButton implements OnTouchListener {

        private final Drawable icon;

        @ColorInt
        private final int pressedColor;

        public VpnCoreButton(Context context) {
            this(context, null);
        }

        public VpnCoreButton(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public VpnCoreButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            setClickable(true);
            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.vpn_flat_button_icon, null);
            setButtonDrawable(icon);
            setGravity(Gravity.BOTTOM);
            pressedColor = ContextCompat.getColor(context, R.color.vpn_button_pressed);
            setOnTouchListener(this);
            setStatus(DISCONNECTED);
        }

        public void setStatus(@ConnectionStatus int status) {
            icon.setLevel(status);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // 4系もサポート。本当は R.drawable.vpn_flat_button_icon に android:tint で色を変えたい
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    CompoundButton view = (CompoundButton) v;
                    icon.getCurrent().setColorFilter(pressedColor, PorterDuff.Mode.SRC_ATOP);
                    view.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    CompoundButton view = (CompoundButton) v;
                    icon.getCurrent().clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return false;
        }
    }
}
