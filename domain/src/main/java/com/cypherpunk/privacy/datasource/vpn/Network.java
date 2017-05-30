package com.cypherpunk.privacy.datasource.vpn;

/**
 * network setting for managing trusted networks
 */
public interface Network {

    String ssid();

    boolean trusted();
}
