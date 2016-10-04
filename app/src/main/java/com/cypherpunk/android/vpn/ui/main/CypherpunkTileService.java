package com.cypherpunk.android.vpn.ui.main;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ProfileManager;
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
        // TODO: check if logged in, permission granted, region selected, etc.
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

        // TODO: check if logged in, permission granted, region selected, etc.
        Intent i = new Intent(MainActivity.TILE_CLICK);
        i.setClass(this, MainActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(MainActivity.TILE_CLICK, true);
        this.startActivity(i);
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, VpnStatus.ConnectionStatus level)
    {
        Tile t = getQsTile();
        if (level == VpnStatus.ConnectionStatus.LEVEL_AUTH_FAILED || level == VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED)
        {
            //t.setState(Tile.STATE_UNAVAILABLE);
            t.setState(Tile.STATE_INACTIVE);
        }
        else
        {
            t.setState(Tile.STATE_ACTIVE);
        }
        t.updateTile();
    }
}
