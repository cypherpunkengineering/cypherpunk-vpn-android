package com.cypherpunk.privacy.ui.vpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;

import javax.inject.Inject;

import timber.log.Timber;

// FIXME: use JobScheduler
public class CypherpunkWifiReceiver extends BroadcastReceiver {

    // FIXME:
    @Nullable
    private static String currentSSID = null;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    VpnSetting vpnSetting;

    @NonNull
    private static String getCurrentWifiSsid(@NonNull Context context) {
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            final String ssid = wifiInfo.getSSID();
            if (!TextUtils.isEmpty(ssid)) {
                return ssid;
            }
        }
        return "";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        CypherpunkApplication.instance.getAppComponent().inject(this);

        final NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (networkInfo == null) {
            return;
        }
        if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
            return;
        }

        switch (networkInfo.getState()) {
            case CONNECTED:
                // check if already got event for this ssid
                if (currentSSID == null) {
                    final String ssid = getCurrentWifiSsid(context);
                    currentSSID = ssid;

                    Timber.d("onWiFiConnected(" + ssid + ")");

                    if (vpnSetting.isAutoSecureUntrusted()) {
                        VpnConnectionIntentService.Mode mode = !TextUtils.isEmpty(ssid) && vpnSetting.isTrusted(ssid)
                                ? VpnConnectionIntentService.Mode.TRUSTED
                                : VpnConnectionIntentService.Mode.UNTRUSTED;
                        context.startService(VpnConnectionIntentService.createIntent(context, mode));
                    }
                }
                break;

            case DISCONNECTED:
                // check if already got event
                if (currentSSID != null) {
                    currentSSID = null;

                    Timber.d("onWiFiDisconnected()");

                    if (vpnSetting.isAutoSecureOther()) {
                        context.startService(VpnConnectionIntentService.createIntent(context,
                                VpnConnectionIntentService.Mode.UNTRUSTED));
                    }
                }
                break;
        }
    }
}
