package com.cypherpunk.privacy.vpn;

import android.util.Log;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.model.Region;

import java.net.InetSocketAddress;
import java.net.Socket;

import io.realm.Realm;

import static com.cypherpunk.privacy.BR.setting;

/**
 * Created by jmaurice on 2017/02/20.
 */

public class ServerPingerThinger extends Thread
{
    private static void log(String str) { Log.w("ServerPingerThinger", str); }

    public InetSocketAddress address;
    public String locationId;

    public int timeout = 10 * 1000;
    public long latency = -2;
    public long delay = 0;

    private Region location;
    private Realm realm;

    public static void pingLocation(Region location)
    {
        if (location.getOvDefault() == null || location.getOvDefault().length() < 7)
        {
            log("Skipping ping for unavailable location " + location.getId());
            return;
        }
        ServerPingerThinger pinger = new ServerPingerThinger();
        pinger.locationId = location.getId();
        pinger.address = new InetSocketAddress(location.getOvDefault(), 443);
        pinger.start();
    }

    @Override
    public void run()
    {
        log("Starting ping for location: " + locationId + " -> " + address.toString());

        try
        {
            // allocate a socket
            Socket sock = new Socket();
            sock.setTcpNoDelay(true);

            // add a delay
            Thread.sleep(delay);

            // protect socket so it doesn't go thru VPN connection
            boolean socketProtected = CypherpunkVPN.protectSocket(sock);

            // calculate latency as time to perform TCP connect()
            long start = System.currentTimeMillis();
            sock.connect(address, timeout);
            long end = System.currentTimeMillis();
            sock.close();
            latency = end - start;
        }
        catch (Exception e)
        {
            log("Exception while pinging location " + locationId + ": " + e.toString());
            return;
        }

        if (latency < 0)
            return;

        log("Location "+ locationId + " ping time: " + latency + "ms");

        // save result in realm
        realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();
        realm.beginTransaction();
        location = realm.where(Region.class).equalTo("id", locationId).findFirst();
        location.setLatency(latency);
        realm.commitTransaction();
    }
}
