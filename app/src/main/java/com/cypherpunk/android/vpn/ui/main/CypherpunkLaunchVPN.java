package com.cypherpunk.android.vpn.ui.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.ui.main.CypherpunkLaunchVPN;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;
import com.cypherpunk.android.vpn.vpn.CypherpunkVpnStatus;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * Created by jmaurice on 2016/10/07.
 */

public class CypherpunkLaunchVPN extends Activity
{
    public static final String AUTO_START = "com.cypherpunk.android.vpn.AUTO_START";
    public static final String TILE_CLICK = "com.cypherpunk.android.vpn.TILE_CLICK";

    private static final int START_VPN_PROFILE = 70;

    private static void log(String str) { Log.w("CypherpunkVPN", str); }

    //private CypherpunkVpnStatus status;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        log("onCreate()");
        super.onCreate(savedInstanceState);
        //status = CypherpunkVpnStatus.getInstance();
        //CypherpunkVPN.getInstance().start(this, this);
        Intent intent = getIntent();
        checkIfAutoStart(intent);
        checkIfTileClick(intent);
        setIntent(null);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        log("onNewIntent()");
        super.onNewIntent(intent);
        checkIfAutoStart(intent);
        checkIfTileClick(intent);
        setIntent(null);
    }

    private void checkIfAutoStart(Intent i)
    {
        log("checkIfAutoStart()");
        if (i != null && i.getBooleanExtra(AUTO_START, false))
        {
            CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
            if (cypherpunkSetting.vpnAutoStartConnect)
            {
                log("auto starting VPN");
                openPermissionDialogIfNeeded();
                CypherpunkVPN.getInstance().start(getApplicationContext(), getBaseContext());
            }
            finish();
        }
    }

    private void checkIfTileClick(Intent i)
    {
        log("checkIfTileClick()");
        if (i != null && i.getBooleanExtra(TILE_CLICK, false))
        {
            openPermissionDialogIfNeeded();
            CypherpunkVPN.getInstance().toggle(getApplicationContext(), getBaseContext());
            moveTaskToBack(true);
        }
    }

    private void openPermissionDialogIfNeeded()
    {
        Intent permissionIntent = VpnService.prepare(this);
        if (permissionIntent != null)
        {
            try
            {
                startActivityForResult(permissionIntent, START_VPN_PROFILE);
            }
            catch (ActivityNotFoundException e)
            {
                finish();
            }
        }
        else
        {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        }
    }

}
