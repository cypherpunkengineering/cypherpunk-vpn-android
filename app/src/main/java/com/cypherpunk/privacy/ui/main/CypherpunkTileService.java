package com.cypherpunk.privacy.ui.main;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;

import de.blinkt.openvpn.core.VpnStatus;

/**
 * Created by jmaurice on 2016/10/04.
 */

@TargetApi(Build.VERSION_CODES.N)
public class CypherpunkTileService extends TileService implements VpnStatus.StateListener
{
    private static void log(String str) { Log.w("CypherpunkVPN", str); }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        CypherpunkVpnStatus status = CypherpunkVpnStatus.getInstance();
        //t.setState(Tile.STATE_UNAVAILABLE);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        VpnStatus.addStateListener(this);
    }

    @Override
    public void onStopListening() {
        VpnStatus.removeStateListener(this);
        super.onStopListening();
    }

    @Override
    public void onClick()
    {
        super.onClick();
        log("CypherpunkTileService.onClick()");

        Intent i = new Intent(CypherpunkLaunchVPN.TILE_CLICK);
        i.setClass(this, CypherpunkLaunchVPN.class);
        i.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_NO_ANIMATION
        );
        i.putExtra(CypherpunkLaunchVPN.TILE_CLICK, true);
        this.startActivity(i);
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, VpnStatus.ConnectionStatus level)
    {
        Tile t = getQsTile();
        if (level == VpnStatus.ConnectionStatus.LEVEL_AUTH_FAILED || level == VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED)
        {
            t.setState(Tile.STATE_INACTIVE);
        }
        else
        {
            t.setState(Tile.STATE_ACTIVE);
        }
        t.updateTile();
    }
}
