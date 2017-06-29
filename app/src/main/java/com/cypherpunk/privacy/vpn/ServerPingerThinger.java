package com.cypherpunk.privacy.vpn;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;

import java.net.InetSocketAddress;
import java.net.Socket;

import timber.log.Timber;

public class ServerPingerThinger extends Thread {

    @NonNull
    private final VpnManager vpnManager;
    @NonNull
    private final VpnServerRepository vpnServerRepository;
    @NonNull
    private final VpnServer vpnServer;

    public ServerPingerThinger(@NonNull VpnServer vpnServer, @NonNull VpnManager vpnManager,
                               @NonNull VpnServerRepository vpnServerRepository) {
        this.vpnServer = vpnServer;
        this.vpnManager = vpnManager;
        this.vpnServerRepository = vpnServerRepository;
    }

    @Override
    public void run() {
        final String regionId = vpnServer.id();
        final InetSocketAddress address = new InetSocketAddress(vpnServer.ovDefault(), 443);
        final int timeout = 5 * 1000;

        boolean socketProtected;

        Timber.d("Starting ping for location: " + regionId + " -> " + address.toString());

        try {
            // allocate a socket or two
            final Socket sock1 = new Socket();
            sock1.setTcpNoDelay(true);

            final Socket sock2 = new Socket();
            sock2.setTcpNoDelay(true);

            // protect sockets so it doesn't go thru VPN connection
            socketProtected = vpnManager.protectSocket(sock1);
            socketProtected = vpnManager.protectSocket(sock2);

            // sleep for a random delay before pinging
            Thread.sleep((long) (Math.random() * 1000));

            // calculate latency as time to perform TCP connect()
            final long s1 = System.currentTimeMillis();
            sock1.connect(address, timeout);
            final long e1 = System.currentTimeMillis();
            sock1.close();

            // do another one, why not
            final long s2 = System.currentTimeMillis();
            sock2.connect(address, timeout);
            final long e2 = System.currentTimeMillis();
            sock2.close();

            // get best of 2 pings
            final long latency = Math.min(e1 - s1, e2 - s2);
            if (latency >= 0) {
                Timber.d("Location " + regionId + " ping time: " + latency + "ms, socket protected: " + socketProtected);
                vpnServerRepository.updateLatency(regionId, latency);
            }

        } catch (Exception e) {
            Timber.e("Exception while pinging location " + regionId + ": " + e.toString());
        }
    }
}
