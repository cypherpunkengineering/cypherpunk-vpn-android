package com.cypherpunk.privacy.domain.model;

import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;

import com.cypherpunk.privacy.datasource.vpn.InternetKillSwitch;
import com.cypherpunk.privacy.datasource.vpn.Network;
import com.cypherpunk.privacy.datasource.vpn.RemotePort;
import com.cypherpunk.privacy.datasource.vpn.TunnelMode;

import java.util.List;

/**
 * vpn settings
 */
public interface VpnSetting {

    //
    // App Settings
    //

    // manage trusted networks

    boolean isAutoSecureUntrusted();

    void updateAutoSecureUntrusted(boolean b);

    boolean isAutoSecureOther();

    void updateAutoSecureOther(boolean b);

    boolean isTrusted(@NonNull String ssid);

    void updateTrusted(@NonNull String ssid, boolean trusted);

    void addNetworks(@NonNull List<WifiConfiguration> networks);

    @NonNull
    List<Network> findAllNetwork();

    //

    boolean isAutoConnect();

    //
    // Privacy Settings
    //

    boolean isBlockMalware();

    boolean isBlockAds();

    @NonNull
    InternetKillSwitch internetKillSwitch();

    void updateInternetKillSwitch(@NonNull InternetKillSwitch internetKillSwitch);

    //
    // Connection Settings
    //

    @NonNull
    TunnelMode tunnelMode();

    void updateTunnelMode(@NonNull TunnelMode tunnelMode);

    @NonNull
    RemotePort remotePort();

    void updateRemotePort(@NonNull RemotePort remotePort);

    @NonNull
    List<String> exceptAppList();

    void updateExceptAppList(@NonNull List<String> exceptAppList);

    boolean allowLanTraffic();

    //
    // Region
    //

    @NonNull
    String regionId();

    void updateRegionId(@NonNull String regionId);

    boolean isCypherplayEnabled();

    void updateCypherplayEnabled(boolean b);

    //
    // analytics
    //

    boolean isAnalyticsEnabled();

    void updateAnalyticsEnabled(boolean b);

}
