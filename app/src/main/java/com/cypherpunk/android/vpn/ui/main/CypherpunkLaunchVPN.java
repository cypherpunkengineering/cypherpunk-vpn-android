package com.cypherpunk.android.vpn.ui.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.cypherpunk.android.vpn.data.api.UserManager;
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
        Intent intent = getIntent();
        launch(intent);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        log("onNewIntent()");
        super.onNewIntent(intent);
        launch(intent);
    }

    private void launch(Intent intent)
    {
        log("launch()");

        // check if user is signed in
        if (!UserManager.isSignedIn() || intent == null)
        {
            log("user not logged in, ignoring launch intent");
            setIntent(null);
            finish();
            return;
        }

        // called from CypherpunkBootReceiver
        if (intent.getBooleanExtra(AUTO_START, false))
        {
            // get user settings
            CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();

            // immediately exit unless user setting for auto start is enabled
            if (!cypherpunkSetting.vpnAutoStartConnect)
            {
                finish();
                return;
            }

            // prepare vpn service and wait for callback
            log("auto starting VPN");
            prepareVpnService();

            // done
            setIntent(null);
            moveTaskToBack(true);
        }
        else if (intent.getBooleanExtra(TILE_CLICK, false))
        {
            // get vpn status
            CypherpunkVpnStatus status = CypherpunkVpnStatus.getInstance();

            // already connected, just disconnect and finish
            if (status.isConnected())
            {
                CypherpunkVPN.getInstance().stop(getApplicationContext(), getBaseContext());
                finish();
                return;
            }

            // prepare vpn service, wait for callback, then connect
            prepareVpnService();

            // done
            setIntent(null);
            moveTaskToBack(true);
        }
    }

    private void prepareVpnService()
    {
        log("prepareVpnService()");

        // returns intent if permission dialog is needed
        Intent permissionIntent = VpnService.prepare(this);

        // no dialog needed, start vpn immediately
        if (permissionIntent == null)
        {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        }
        else // dialog needed, start intent and wait for callback
        {
            try
            {
                startActivityForResult(permissionIntent, START_VPN_PROFILE);
            }
            catch (ActivityNotFoundException e) // buggy sony devices
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        log("onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case START_VPN_PROFILE:
                if (resultCode == RESULT_OK)
                    CypherpunkVPN.getInstance().start(getApplicationContext(), getBaseContext());
                finish();
                break;
        }
    }
}
