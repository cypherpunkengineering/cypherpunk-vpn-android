package com.cypherpunk.privacy.ui.vpn;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.NonNull;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.vpn.VpnStatusHolder;

import javax.inject.Inject;

import de.blinkt.openvpn.core.VpnStatus;
import timber.log.Timber;

import static com.cypherpunk.privacy.ui.vpn.VpnConnectionIntentService.Mode.TILE;

@TargetApi(Build.VERSION_CODES.N)
public class CypherpunkTileService extends TileService implements VpnStatusHolder.StateListener {

    @Inject
    VpnStatusHolder vpnStatusHolder;

    @Override
    public void onCreate() {
        super.onCreate();
        CypherpunkApplication.instance.getAppComponent().inject(this);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        vpnStatusHolder.addListener(this);
    }

    @Override
    public void onStopListening() {
        vpnStatusHolder.removeListener(this);
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        Timber.d("onClick()");
        startService(VpnConnectionIntentService.createIntent(this, TILE));
    }

    @Override
    public void onStateChanged(@NonNull VpnStatus.ConnectionStatus status) {
        final Tile t = getQsTile();
        switch (status) {
            case LEVEL_AUTH_FAILED:
            case LEVEL_NOT_CONNECTED:
                t.setState(Tile.STATE_INACTIVE);
                break;
            default:
                t.setState(Tile.STATE_ACTIVE);
                break;
        }
        t.updateTile();
    }
}
