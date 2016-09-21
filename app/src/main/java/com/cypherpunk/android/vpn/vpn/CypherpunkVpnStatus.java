package com.cypherpunk.android.vpn.vpn;

import android.support.annotation.NonNull;

import de.blinkt.openvpn.core.VpnStatus;


public class CypherpunkVpnStatus implements VpnStatus.StateListener {

    private static VpnStatus.ConnectionStatus level
            = VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED;
    private static CypherpunkVpnStatus singleton;
    private long connectedTime;

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
        if (level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED) {
            connectedTime = System.currentTimeMillis();
        }
    }

    public boolean isConnected() {
        return level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED;
    }

    public boolean isDisconnected() {
        return level == VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED || level == VpnStatus.ConnectionStatus.LEVEL_NONETWORK;
    }

    public long getConnectedTime() {
        return connectedTime;
    }
}
