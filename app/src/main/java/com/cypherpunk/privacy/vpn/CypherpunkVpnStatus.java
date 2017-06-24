package com.cypherpunk.privacy.vpn;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;

import java.util.Date;

import javax.inject.Inject;

import de.blinkt.openvpn.core.VpnStatus;


public class CypherpunkVpnStatus implements VpnStatus.StateListener {

    private static VpnStatus.ConnectionStatus level
            = VpnStatus.ConnectionStatus.LEVEL_NOT_CONNECTED;
    private static CypherpunkVpnStatus singleton;
    private long connectedTime;
    private String originalIp;
    private String newIp;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    @NonNull
    public static synchronized CypherpunkVpnStatus getInstance() {
        if (singleton == null) {
            singleton = new CypherpunkVpnStatus();
            VpnStatus.addStateListener(singleton);
        }
        return singleton;
    }

    public CypherpunkVpnStatus() {
        CypherpunkApplication.instance.getAppComponent().inject(this);
    }

    @Override
    public void updateState(String state, String logmessage,
                            int localizedResId, VpnStatus.ConnectionStatus level) {
        CypherpunkVpnStatus.level = level;
        if (level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED) {
            connectedTime = System.currentTimeMillis();
            vpnServerRepository.updateLastConnectedDate(vpnSetting.regionId(), new Date());
        }
    }

    public boolean isConnected() {
        return level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED;
    }

    public boolean isDisconnected() {
        return level == VpnStatus.ConnectionStatus.LEVEL_NOT_CONNECTED || level == VpnStatus.ConnectionStatus.LEVEL_NO_NETWORK;
    }

    public long getConnectedTime() {
        return connectedTime;
    }

    public String getOriginalIp() {
        return originalIp;
    }

    public void setOriginalIp(String originalIp) {
        this.originalIp = originalIp;
    }

    public String getNewIp() {
        return newIp;
    }

    public void setNewIp(String newIp) {
        this.newIp = newIp;
    }
}
