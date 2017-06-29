package com.cypherpunk.privacy.ui.vpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

import static com.cypherpunk.privacy.ui.vpn.VpnConnectionIntentService.Mode.AUTO_START;

public class BootAndPackageReplaceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        Timber.d("onReceive() : " + action);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            context.startService(VpnConnectionIntentService.createIntent(context, AUTO_START));
        }
    }
}
