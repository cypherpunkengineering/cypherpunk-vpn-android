package com.cypherpunk.privacy.domain.model.vpn;

import android.support.annotation.NonNull;

/**
 * vpn server properties
 */
public interface VpnServer {
    @NonNull
    String id();

    @NonNull
    String name();

    @NonNull
    String country();

    @NonNull
    Level level();

    boolean authorized();

    @NonNull
    String ovHostname();

    @NonNull
    String ovDefault();

    @NonNull
    String ovNone();

    @NonNull
    String ovStrong();

    @NonNull
    String ovStealth();

    boolean isSelectable();

    boolean favorite();
}
