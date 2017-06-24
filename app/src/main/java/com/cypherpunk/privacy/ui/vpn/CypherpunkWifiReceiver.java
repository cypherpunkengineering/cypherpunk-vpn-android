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

/**
 * Created by jmaurice on 2016/10/12.
 */
public class CypherpunkWifiReceiver extends BroadcastReceiver {

    // FIXME:
    @Nullable
    private static String currentSSID = null;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    VpnSetting vpnSetting;

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
                final String ssid = getCurrentWifiSSID(context);
                if (currentSSID == null) {
                    currentSSID = ssid;

                    Timber.d("onWiFiConnected(" + ssid + ")");

                    if (vpnSetting.isAutoSecureUntrusted()) {
                        startIntent(context, !TextUtils.isEmpty(ssid) && vpnSetting.isTrusted(ssid)
                                ? CypherpunkLaunchVPN.NETWORK_TRUSTED
                                : CypherpunkLaunchVPN.NETWORK_UNTRUSTED);
                    }
                }
                break;
            case DISCONNECTED:
                // check if already got event
                if (currentSSID != null) {
                    currentSSID = null;

                    Timber.d("onWiFiDisconnected()");

                    if (vpnSetting.isAutoSecureOther()) {
                        startIntent(context, CypherpunkLaunchVPN.NETWORK_UNTRUSTED);
                    }
                }
                break;
        }
    }

    @NonNull
    private String getCurrentWifiSSID(@NonNull Context context) {
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        final WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null) {
            final String ssid = info.getSSID();
            if (!TextUtils.isEmpty(ssid)) {
                return ssid;
            }
        }
        return "";
    }

    private void startIntent(@NonNull Context context, @NonNull String intention) {
        Timber.d("startIntent()");
        final Intent intent = new Intent(CypherpunkLaunchVPN.AUTO_START);
        intent.setClass(context, CypherpunkLaunchVPN.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(intention, true);
        context.startActivity(intent);
    }
}
