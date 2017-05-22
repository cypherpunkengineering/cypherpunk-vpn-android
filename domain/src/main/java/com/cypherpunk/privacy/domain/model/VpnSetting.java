package com.cypherpunk.privacy.domain.model;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * vpn settings
 */
public interface VpnSetting {

    //
    // App Settings
    //

    boolean isAutoSecureUntrusted();

    void updateAutoSecureUntrusted(boolean b);

    boolean isAutoSecureOther();

    void updateAutoSecureOther(boolean b);

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
