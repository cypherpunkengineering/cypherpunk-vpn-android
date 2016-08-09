package com.cypherpunk.android.vpn.vpn;

import android.support.annotation.NonNull;

import de.blinkt.openvpn.core.VpnStatus;


public class CypherpunkVpnStatus implements VpnStatus.StateListener {

    private static VpnStatus.ConnectionStatus level
            = VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED;
    private static CypherpunkVpnStatus singleton;

    @NonNull
    public static synchronized CypherpunkVpnStatus getInstance() {
        if (singleton == null) {
            singleton = new CypherpunkVpnStatus();
            VpnStatus.addStateListener(singleton);
        }
        return singleton;
    }

    @Override
    public void updateState(String state, String logmessage,
                            int localizedResId, VpnStatus.ConnectionStatus level) {
        CypherpunkVpnStatus.level = level;
    }

    public boolean isConnected() {
        return level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED;
    }

    public boolean isDisconnected() {
        return level == VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED;
    }
}
