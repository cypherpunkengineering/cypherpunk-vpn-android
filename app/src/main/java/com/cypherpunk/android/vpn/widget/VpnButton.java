package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.cypherpunk.android.vpn.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class VpnButton extends CompoundButton implements CompoundButton.OnCheckedChangeListener {

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    private OnCheckedChangeListener listener;

    private Drawable icon;
    private Drawable frame;
    private int connectedFrameColor;
    private int connectingFrameColor;
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

        if (isInEditMode()) {
            return;
        }

        setClickable(true);
        setBackgroundResource(R.drawable.vpn_button);
        setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.vpn_button_icon_padding));
        setOnCheckedChangeListener(this);

        frame = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.vpn_button_frame));
        icon = ResourcesCompat.getDrawable(getResources(), R.drawable.vpn_button_icon, null);

        disconnectedFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_disconnected);
        connectingFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_connecting);
        connectedFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_connected);

        setStatus(DISCONNECTED);

        // TODO: pressed color
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (listener != null) {
            listener.onCheckedChanged(compoundButton, isChecked);
        }
    }

    public void setStatus(@ConnectionStatus int status) {
        switch (status) {
            case DISCONNECTED:
                DrawableCompat.setTint(frame, disconnectedFrameColor);
                break;
            case CONNECTING:
                DrawableCompat.setTint(frame, connectingFrameColor);
                break;
            case CONNECTED:
                DrawableCompat.setTint(frame, connectedFrameColor);
                break;
        }
        icon.setLevel(status);
        LayerDrawable background = (LayerDrawable) getBackground();
        background.setDrawableByLayerId(R.id.button_frame, frame);
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
