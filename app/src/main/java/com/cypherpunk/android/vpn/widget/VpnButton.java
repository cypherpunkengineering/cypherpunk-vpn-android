package com.cypherpunk.android.vpn.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.cypherpunk.android.vpn.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class VpnButton extends FrameLayout {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    private CompoundButton.OnCheckedChangeListener listener;

    private VpnCoreButton buttonView;
    private View frameView;
    private Drawable frame;
    private Drawable drawable;
    private int connectedFrameColor;
    private int disconnectedFrameColor;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionStatus {
    }

    public VpnButton(Context context) {
        this(context, null);
    }

    public VpnButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VpnButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        buttonView = new VpnCoreButton(context);
        frameView = new View(context);
        addView(frameView);
        addView(buttonView);

        frame = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.vpn_button_frame));
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.vpn_button_border_orange, null);
        buttonView.setOnCheckedChangeListener(new VpnCoreButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("VpnButton", "b ischeck");
                if (listener != null) {
                    listener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });
        disconnectedFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_disconnected);
        connectedFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_connected);

        setClickable(true);
        setStatus(DISCONNECTED);
    }

    public void setStatus(@ConnectionStatus int status) {
        buttonView.setStatus(status);
        switch (status) {
            case DISCONNECTED:
                DrawableCompat.setTint(frame, disconnectedFrameColor);
                frameView.setBackground(frame);
                break;
            case CONNECTING:
                frameView.setBackground(drawable);
                ObjectAnimator anim = ObjectAnimator.ofFloat(frameView, "rotation", 0f, 360f);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(3000);
                anim.setRepeatCount(ValueAnimator.INFINITE);
                anim.start();
                break;
            case CONNECTED:
                DrawableCompat.setTint(frame, connectedFrameColor);
                frameView.setBackground(frame);

                break;
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            binding.frame.setBackground(frame);
//        } else {
//            binding.frame.setBackgroundDrawable(frame);
//        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public static class VpnCoreButton extends CompoundButton implements CompoundButton.OnCheckedChangeListener {

        public interface OnCheckedChangeListener {
            void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
        }

        private OnCheckedChangeListener listener;

        private Drawable icon;

        public VpnCoreButton(Context context) {
            this(context, null);
        }

        public VpnCoreButton(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public VpnCoreButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            setClickable(true);
            setBackgroundResource(R.drawable.vpn_button);
            setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.vpn_button_icon_padding));
            setOnCheckedChangeListener(this);

            icon = ResourcesCompat.getDrawable(getResources(), R.drawable.vpn_button_icon, null);

            setStatus(DISCONNECTED);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (listener != null) {
                listener.onCheckedChanged(compoundButton, isChecked);
            }
        }

        public void setStatus(@ConnectionStatus int status) {
            icon.setLevel(status);
        }

        public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // draw io icon
            if (icon != null) {
                final int height = icon.getIntrinsicHeight();
                int y = getHeight() - height - getPaddingBottom();
                int buttonWidth = icon.getIntrinsicWidth();
                int buttonLeft = (getWidth() - buttonWidth) / 2;
                icon.setBounds(buttonLeft, y, buttonLeft + buttonWidth, y + height);
                icon.draw(canvas);
            }
        }
    }
}
