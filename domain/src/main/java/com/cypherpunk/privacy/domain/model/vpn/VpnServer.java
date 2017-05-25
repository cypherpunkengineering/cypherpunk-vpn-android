package com.cypherpunk.privacy.domain.model.vpn;

import android.support.annotation.NonNull;

/**
 * vpn server properties
 */
public interface VpnServer {
    @NonNull
    String getId();

    @NonNull
    String getCountry();

    @NonNull
    String getRegionName();

    String getLevel();

    String getOvHostname();

    String getOvNone();

    String getOvDefault();

    String getOvStrong();

    String getOvStealth();

    boolean favorite();

    boolean authorized();
}
