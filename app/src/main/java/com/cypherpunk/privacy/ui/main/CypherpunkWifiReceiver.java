package com.cypherpunk.privacy.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.model.Network;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by jmaurice on 2016/10/12.
 */

public class CypherpunkWifiReceiver extends BroadcastReceiver {
    private static String currentSSID = null;

    @Inject
    Realm realm;

    @Inject
    VpnSetting vpnSetting;

    @Override
    public void onReceive(Context context, Intent intent) {
        CypherpunkApplication.instance.getAppComponent().inject(this);

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            switch (networkInfo.getState()) {
                case CONNECTED:
                    // check if already got event for this ssid
                    String ssid = getCurrentWifiSSID(context);
                    if (currentSSID == null) {
                        currentSSID = ssid;
                        onWifiConnected(context);
                    }
                    break;
                case DISCONNECTED:
                    // check if already got event
                    if (currentSSID != null) {
                        currentSSID = null;
                        onWifiDisconnected(context);
                    }
                    break;
            }
        }
    }

    private void onWifiConnected(Context context) {
        String ssid = getCurrentWifiSSID(context);
        Timber.d("onWiFiConnected(" + ssid + ")");

        boolean trustedNetwork = isWifiTrusted(ssid);

        if (vpnSetting.isAutoSecureUntrusted()) {
            if (trustedNetwork)
                startIntent(context, CypherpunkLaunchVPN.NETWORK_TRUSTED);
            else
                startIntent(context, CypherpunkLaunchVPN.NETWORK_UNTRUSTED);
        }
    }

    private void onWifiDisconnected(Context context) {
        Timber.d("onWiFiDisconnected()");

        if (vpnSetting.isAutoSecureOther()) {
            startIntent(context, CypherpunkLaunchVPN.NETWORK_UNTRUSTED);
        }
    }

    private boolean isWifiTrusted(String ssid) {
        boolean trustedNetwork = false;

        Realm realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();
        RealmResults<Network> networks = realm.where(Network.class).findAll();
        for (Network network : networks) {
            String quotedSSID = '"' + network.getSsid() + '"';
            if (quotedSSID.equals(ssid)) {
                if (network.isTrusted()) {
                    Timber.d("SSID " + ssid + " is trusted");
                    trustedNetwork = true;
                    break;
                } else {
                    Timber.d("SSID " + ssid + " is NOT trusted");
                    trustedNetwork = false;
                    break;
                }
            }
        }

        return trustedNetwork;
    }

    private String getCurrentWifiSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();

        if (ssid == null || ssid.length() < 1)
            return "";

        return ssid;
    }

    private void startIntent(Context context, String intention) {
        Timber.d("startIntent()");
        Intent i = new Intent(CypherpunkLaunchVPN.AUTO_START);
        i.setClass(context, CypherpunkLaunchVPN.class);
        i.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_NO_HISTORY
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION
        );
        i.putExtra(intention, true);
        context.startActivity(i);
    }

    //CypherpunkVPN.getInstance().stop(context, context);
}
