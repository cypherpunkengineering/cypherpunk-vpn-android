package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class VpnSwitch extends SwitchCompat {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

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
        setStatus(DISCONNECTED);
    }

    public void setStatus(@ConnectionStatus int status) {
        switch (status) {
            case DISCONNECTED:
                break;
            case CONNECTING:
                break;
            case CONNECTED:
                break;
        }
    }
}
