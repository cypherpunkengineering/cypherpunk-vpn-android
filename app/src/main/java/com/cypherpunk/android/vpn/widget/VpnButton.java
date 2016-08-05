package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ViewVpnButtonBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class VpnButton extends FrameLayout implements CompoundButton.OnCheckedChangeListener {

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    private OnCheckedChangeListener listener;
    private ViewVpnButtonBinding binding;

    private Drawable disconnectedIconDrawable;
    private Drawable connectingIconDrawable;
    private Drawable connectedIconDrawable;
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

        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_vpn_button, this, true);

        frame = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.vpn_button_frame));

        disconnectedIconDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.io_btn_red, null);
        connectingIconDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.io_btn_orange, null);
        connectedIconDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.io_btn_green, null);

        disconnectedFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_disconnected);
        connectingFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_connecting);
        connectedFrameColor = ContextCompat.getColor(context, R.color.vpn_button_frame_connected);

        setStatus(DISCONNECTED);

        binding.button.setOnCheckedChangeListener(this);

        // TODO: pressed color
        // TODO: [bug] if pressed, icon is gone
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (listener != null) {
            listener.onCheckedChanged(compoundButton, isChecked);
        }
    }

    public void setStatus(@ConnectionStatus int status) {
        Drawable icon = null;
        switch (status) {
            case DISCONNECTED:
                icon = disconnectedIconDrawable;
                DrawableCompat.setTint(frame, disconnectedFrameColor);
                break;
            case CONNECTING:
                icon = connectingIconDrawable;
                DrawableCompat.setTint(frame, connectingFrameColor);
                break;
            case CONNECTED:
                icon = connectedIconDrawable;
                DrawableCompat.setTint(frame, connectedFrameColor);
                break;
        }
        binding.icon.setImageDrawable(icon);
        LayerDrawable background = (LayerDrawable) binding.button.getBackground();
        background.setDrawableByLayerId(R.id.button_frame, frame);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }
}
