package com.cypherpunk.privacy.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cypherpunk.privacy.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class VpnButton extends FrameLayout {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    private CompoundButton.OnCheckedChangeListener listener;

    private ObjectAnimator anim;
    private VpnCoreButton buttonView;
    private Drawable frame;

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

        ImageView frameView = new ImageView(context);
        addView(frameView);
        buttonView = new VpnCoreButton(context);
        addView(buttonView);

        frame = ResourcesCompat.getDrawable(getResources(), R.drawable.vpn_button_frame, null);
        frameView.setImageDrawable(frame);

        buttonView.setOnCheckedChangeListener(new VpnCoreButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });

        anim = ObjectAnimator.ofFloat(frameView, "rotation", 0f, 360f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(3000);
        anim.setRepeatCount(ValueAnimator.INFINITE);

        setClickable(true);
        setStatus(DISCONNECTED);
    }

    public void setStatus(@ConnectionStatus int status) {
        switch (status) {
            case DISCONNECTED:
            case CONNECTED:
                anim.cancel();
                break;
            case CONNECTING:
                anim.start();
                break;
        }
        buttonView.setStatus(status);
        frame.setLevel(status);
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
            invalidate();
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
