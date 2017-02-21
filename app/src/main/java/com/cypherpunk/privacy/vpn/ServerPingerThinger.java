package com.cypherpunk.privacy.vpn;

import android.util.Log;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.model.Region;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import io.realm.Realm;
import io.realm.Sort;

import static com.cypherpunk.privacy.BR.setting;

/**
 * Created by jmaurice on 2017/02/20.
 */

public class ServerPingerThinger extends Thread
{
    public InetSocketAddress address;
    public String locationId;

    private static void log(String str) { Log.w("ServerPingerThinger", str); }

    public static boolean isPingable(Region location)
    {
        if (location.getOvDefault().length() < 7)
            return false;

        if (location.isAuthorized() == false)
            return false;

        return true;
    }

    public static void pingLocation(Region location)
    {
        if (isPingable(location) == false)
        {
            log("Skipping ping for location " + location.getId());

            // update latency to -1 when locations become unavailable
            updateLocationLatency(location.getId(), -1);

            return;
        }
        ServerPingerThinger pinger = new ServerPingerThinger();
        pinger.locationId = location.getId();
        pinger.address = new InetSocketAddress(location.getOvDefault(), 443);
        pinger.start();
    }

    public static Region getFastestLocation()
    {
        // select to fastest location
        Realm realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();
        Region fastestLocation = null;

        try // first get locations which have valid latency data
        {
            fastestLocation = realm.where(Region.class)
                    .equalTo("authorized", true)
                    .contains("ovDefault", ".")
                    .notEqualTo("level", "developer")
                    .notEqualTo("latency", -1)
                    .findAllSorted("latency", Sort.ASCENDING)
                    .first();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
        catch (Exception e)
        {
        }

        // if no latency data, just return any location
        if (fastestLocation == null)
        {
            try
            {
                fastestLocation = realm.where(Region.class)
                        .equalTo("authorized", true)
                        .contains("ovDefault", ".")
                        .notEqualTo("level", "developer")
                        .findAllSorted("latency", Sort.ASCENDING)
                        .first();
            }
            catch (IndexOutOfBoundsException e)
            {
            }
            catch (Exception e)
            {
            }
        }

        // dont forget to close realm
        realm.close();

        // however, might still be null if no locations available
        return fastestLocation;
    }

    private static void updateLocationLatency(String locationId, long latency)
    {
        Realm realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();
        realm.beginTransaction();

        // save result in realm
        Region location = realm.where(Region.class).equalTo("id", locationId).findFirst();
        location.setLatency(latency);
        realm.commitTransaction();

        // dont forget to close realm
        realm.close();
    }

    @Override
    public void run()
    {
        int timeout = 5 * 1000;
        long latency, l1, l2;
        boolean socketProtected = false;

        log("Starting ping for location: " + locationId + " -> " + address.toString());

        try
        {
            // allocate a socket or two
            Socket sock1 = new Socket();
            sock1.setTcpNoDelay(true);
            Socket sock2 = new Socket();
            sock2.setTcpNoDelay(true);

            // protect sockets so it doesn't go thru VPN connection
            socketProtected = CypherpunkVPN.protectSocket(sock1);
            socketProtected = CypherpunkVPN.protectSocket(sock2);

            // sleep for a random delay before pinging
            Thread.sleep((long)(Math.random() * 1000));

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
        }
        catch (Exception e)
        {
            log("Exception while pinging location " + locationId + ": " + e.toString());
            return;
        }

        if (latency < 0)
            return;

        log("Location " + locationId + " ping time: " + latency + "ms, socket protected: "+socketProtected);
        updateLocationLatency(locationId, latency);
    }
}
