package com.cypherpunk.privacy.ui.vpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

/**
 * Created by jmaurice on 2016/09/29.
 */

public class CypherpunkBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive()");

        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            startIntent(context);
        }
    }

    private void startIntent(Context context) {
        Timber.d("startIntent()");
        Intent i = new Intent(CypherpunkLaunchVPN.AUTO_START);
        i.setClass(context, CypherpunkLaunchVPN.class);
        i.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_NO_HISTORY
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION
        );
        i.putExtra(CypherpunkLaunchVPN.AUTO_START, true);
        context.startActivity(i);
    }
}
