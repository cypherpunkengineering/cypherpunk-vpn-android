package com.cypherpunk.privacy.vpn;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.domain.model.vpn.VpnServer;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by jmaurice on 2017/02/20.
 */

public class ServerPingerThinger extends Thread {
    private final VpnServerRepository vpnServerRepository;
    public InetSocketAddress address;
    public String locationId;

    public ServerPingerThinger(VpnServerRepository vpnServerRepository) {
        this.vpnServerRepository = vpnServerRepository;
    }

    public static boolean isPingable(VpnServer vpnServer) {
        return vpnServer.ovDefault().length() >= 7 && vpnServer.authorized();
    }

    public static void pingLocation(VpnServer vpnServer, @NonNull VpnServerRepository vpnServerRepository) {
        if (!isPingable(vpnServer)) {
            Timber.d("Skipping ping for location " + vpnServer.id());

            // update latency to -1 when locations become unavailable
            vpnServerRepository.updateLatency(vpnServer.id(), -1);
            return;
        }

        ServerPingerThinger pinger = new ServerPingerThinger(vpnServerRepository);
        pinger.locationId = vpnServer.id();
        pinger.address = new InetSocketAddress(vpnServer.ovDefault(), 443);
        pinger.start();
    }

    @Override
    public void run() {
        int timeout = 5 * 1000;
        long latency, l1, l2;
        boolean socketProtected = false;

        Timber.d("Starting ping for location: " + locationId + " -> " + address.toString());

        try {
            // allocate a socket or two
            Socket sock1 = new Socket();
            sock1.setTcpNoDelay(true);
            Socket sock2 = new Socket();
            sock2.setTcpNoDelay(true);

            // protect sockets so it doesn't go thru VPN connection
            socketProtected = CypherpunkVPN.protectSocket(sock1);
            socketProtected = CypherpunkVPN.protectSocket(sock2);

            // sleep for a random delay before pinging
            Thread.sleep((long) (Math.random() * 1000));

            // calculate latency as time to perform TCP connect()
            long s1 = System.currentTimeMillis();
            sock1.connect(address, timeout);
            long e1 = System.currentTimeMillis();
            sock1.close();

            // do another one, why not
            long s2 = System.currentTimeMillis();
            sock2.connect(address, timeout);
            long e2 = System.currentTimeMillis();
            sock2.close();

            // get best of 2 pings
            l1 = e1 - s1;
            l2 = e2 - s2;
            List<Long> list = Arrays.asList(l1, l2);
            latency = Collections.min(list);
        } catch (Exception e) {
            Timber.e("Exception while pinging location " + locationId + ": " + e.toString());
            return;
        }

        if (latency < 0) {
            return;
        }

        Timber.d("Location " + locationId + " ping time: " + latency + "ms, socket protected: " + socketProtected);
        vpnServerRepository.updateLatency(locationId, latency);
    }
}
