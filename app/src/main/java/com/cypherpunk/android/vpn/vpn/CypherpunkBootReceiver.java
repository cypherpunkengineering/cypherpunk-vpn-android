package com.cypherpunk.android.vpn.vpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cypherpunk.android.vpn.ui.main.MainActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;

/**
 * Created by jmaurice on 2016/09/29.
 */

public class CypherpunkBootReceiver extends BroadcastReceiver
{
    private static void log(String str) {
        Log.w("CypherpunkVPN", str);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();

        log("onReceive()");

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED))
        {
            log("onReceive() -> startActivity()");
            Intent i = new Intent(MainActivity.AUTO_START);
            i.setClass(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(MainActivity.AUTO_START, true);
            context.startActivity(i);
        }
    }
}
