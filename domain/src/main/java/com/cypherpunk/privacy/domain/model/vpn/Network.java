package com.cypherpunk.privacy.domain.model.vpn;

/**
 * network setting for managing trusted networks
 */
public interface Network {

    String ssid();

    boolean trusted();
}
