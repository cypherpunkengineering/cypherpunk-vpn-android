package com.cypherpunk.privacy.ui.vpn;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.NonNull;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.vpn.VpnStatusHolder;

import javax.inject.Inject;

import de.blinkt.openvpn.core.VpnStatus;

import static com.cypherpunk.privacy.ui.vpn.VpnConnectionIntentService.Mode.TILE;

@TargetApi(Build.VERSION_CODES.N)
public class CypherpunkTileService extends TileService implements VpnStatusHolder.StateListener {

    @Inject
    VpnStatusHolder vpnStatusHolder;

    @Inject
    AccountSetting accountSetting;

    @Override
    public void onCreate() {
        super.onCreate();
        CypherpunkApplication.instance.getAppComponent().inject(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile(vpnStatusHolder.status());
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
        startService(VpnConnectionIntentService.createIntent(this, TILE));
    }

    private void updateTile(@NonNull VpnStatus.ConnectionStatus status) {
        final Resources res = getApplicationContext().getResources();

        final Tile t = getQsTile();

        if (!accountSetting.isSignedIn() || accountSetting.isPending() || !accountSetting.isActive()) {
            t.setState(Tile.STATE_UNAVAILABLE);
            t.setLabel(res.getString(R.string.app_name));
            t.updateTile();
            return;
        }

        switch (status) {
            case LEVEL_CONNECTED:
                t.setLabel(res.getString(R.string.status_connected));
                break;

            case LEVEL_START:
            case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
            case LEVEL_CONNECTING_SERVER_REPLIED:
                t.setState(Tile.STATE_ACTIVE);
                t.setLabel(res.getString(R.string.status_connecting));
                break;

            case LEVEL_NOT_CONNECTED:
                t.setState(Tile.STATE_INACTIVE);
                t.setLabel(res.getString(R.string.status_disconnected));
                break;

            default:
                t.setState(Tile.STATE_UNAVAILABLE);
                t.setLabel(res.getString(R.string.app_name));
                break;
        }
        t.updateTile();
    }

    @Override
    public void onStateChanged(@NonNull VpnStatus.ConnectionStatus status) {
        updateTile(status);
    }
}
