package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import com.cypherpunk.android.vpn.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class VpnSwitch extends SwitchCompat {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    private Drawable drawableConnectThumb;
    private Drawable drawableConnectTrack;
    private int colorConnectThumb;
    private int colorConnectTrack;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionStatus {
    }

    public VpnSwitch(Context context) {
        this(context, null);
    }

    public VpnSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VpnSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        drawableConnectThumb = ResourcesCompat.getDrawable(getResources(), R.drawable.switch_thumb_solid, null);
        drawableConnectTrack = ResourcesCompat.getDrawable(getResources(), R.drawable.switch_track_solid, null);
        colorConnectThumb = ContextCompat.getColor(context, R.color.switch_thumb_connect);
        colorConnectTrack = ContextCompat.getColor(context, R.color.switch_track_connect);

        // エラー回避
        setClickable(true);
        setTextOn("");
        setTextOff("");

        setStatus(DISCONNECTED);
    }

    public void setStatus(@ConnectionStatus int status) {
        if (status == CONNECTED) {
            drawableConnectThumb.setColorFilter(colorConnectThumb, PorterDuff.Mode.SRC_ATOP);
            drawableConnectTrack.setColorFilter(colorConnectTrack, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawableConnectThumb.clearColorFilter();
            drawableConnectTrack.clearColorFilter();
        }
        LayerDrawable thumbDrawable = (LayerDrawable) getThumbDrawable();
        LayerDrawable trackDrawable = (LayerDrawable) getTrackDrawable();
        thumbDrawable.setDrawableByLayerId(R.id.thumb, drawableConnectThumb);
        trackDrawable.setDrawableByLayerId(R.id.track, drawableConnectTrack);
    }
}
