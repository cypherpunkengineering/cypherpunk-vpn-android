package com.cypherpunk.android.vpn.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVpnManagementThread;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * Created by jmaurice on 7/12/16.
 */
public class CypherpunkVPN
{
    private static String username;
    private static String password;
    private static ConfigParser cp;
    private static VpnProfile vpnProfile;
    private static String conf;

    private static OpenVPNService service = null;

    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) iBinder;
            service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    private static void log(String str)
    {
        Log.w("CypherpunkVPN", str);
    }

    public static void start(final Context context, final Context baseContext)
    {
        log("start()");

        Intent intent = new Intent(baseContext, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);

        try
        {
            conf = generateConfig(context);
            cp = new ConfigParser();
            cp.parseConfig(new StringReader(conf));
            vpnProfile = cp.convertProfile();
            ProfileManager.setTemporaryProfile(vpnProfile);
            //log("vpn profile check: "+vpnProfile.checkProfile(context));
        }
        catch (Exception E)
        {
            return;
        }

        new Thread()
        {
            @Override
            public void run()
            {
                log("new Thread() -> VPNLaunchHelper()");
                VPNLaunchHelper.startOpenVpn(vpnProfile, baseContext);
            }
        }.start();
    }

    public static void stop(final Context context, final Context baseContext)
    {
        log("stop()");
        if (service != null)
        {
            service.getManagement().stopVPN(false);
        }
    }

    private static String generateConfig(Context context)
    {
        log("generateConfig()");

        InputStream is;
        InputStreamReader isr;
        BufferedReader br;

        try {
            is = context.getAssets().open("openvpn.conf");
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
        }
        catch (Exception e)
        {
            log("unable to generate config: " + e.toString());
            return null;
        }

        List<String> list = new ArrayList<String>();
        String line;


        try
        {
            while ((line = br.readLine()) != null)
            {
                list.add(line);
            }
        }
        catch (Exception e)
        {
            return null;
        }

        list.add("remote 208.111.52.1 7133\n");

        String[] conf = list.toArray(new String[0]);

        return TextUtils.join("\n", conf);

    }
}
