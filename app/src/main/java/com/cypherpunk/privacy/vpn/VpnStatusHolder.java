package com.cypherpunk.privacy.vpn;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.blinkt.openvpn.core.VpnStatus;

public class VpnStatusHolder implements VpnStatus.StateListener {

    public interface StateListener {
        void onStateChanged(@NonNull VpnStatus.ConnectionStatus status);
    }

    private final List<StateListener> listeners = new ArrayList<>();
    private final VpnSetting vpnSetting;
    private final VpnServerRepository vpnServerRepository;

    @NonNull
    private VpnStatus.ConnectionStatus status = VpnStatus.ConnectionStatus.LEVEL_NOT_CONNECTED;

    public VpnStatusHolder(@NonNull VpnSetting vpnSetting, @NonNull VpnServerRepository vpnServerRepository) {
        this.vpnSetting = vpnSetting;
        this.vpnServerRepository = vpnServerRepository;

        VpnStatus.addStateListener(this);
    }

    public void addListener(@NonNull StateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(@NonNull StateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, VpnStatus.ConnectionStatus status) {
        this.status = status;
        if (status == VpnStatus.ConnectionStatus.LEVEL_CONNECTED) {
            vpnServerRepository.updateLastConnectedDate(vpnSetting.regionId(), new Date());
        }

        for (StateListener listener : listeners) {
            listener.onStateChanged(status);
        }
    }

    @NonNull
    public VpnStatus.ConnectionStatus status() {
        return status;
    }

    public boolean isConnected() {
        switch (status) {
            case LEVEL_CONNECTED:
                return true;
        }
        return false;
    }

    public boolean isDisconnected() {
        switch (status) {
            case LEVEL_NOT_CONNECTED:
            case LEVEL_NO_NETWORK:
                return true;
        }
        return false;
    }
}
