package com.cypherpunk.android.vpn.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class VpnFlatButton extends CompoundButton {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    private Drawable icon;
    private ObjectAnimator anim;

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

        icon = ResourcesCompat.getDrawable(getResources(), R.drawable.vpn_flat_button_icon, null);
        setGravity(Gravity.BOTTOM);
        setButtonDrawable(icon);

        anim = ObjectAnimator.ofFloat(icon, "rotation", 0f, 360f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setTarget(icon);
        anim.setDuration(3000);
        anim.setRepeatCount(ValueAnimator.INFINITE);

        setStatus(DISCONNECTED);
    }

    public void setStatus(@ConnectionStatus int status) {
        icon.setLevel(status);
        switch (status) {
            case DISCONNECTED:
            case CONNECTED:
                anim.cancel();
                break;
            case CONNECTING:
                anim.start();
                break;
        }
    }
}
