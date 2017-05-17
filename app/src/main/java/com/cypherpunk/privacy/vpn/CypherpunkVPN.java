package com.cypherpunk.privacy.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.android.annotations.NonNull;
import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.Region;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNManagement;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by jmaurice on 7/12/16.
 */
public class CypherpunkVPN {
    private static CypherpunkVPN singleton;

    private String username;
    private String password;
    private ConfigParser cp;
    private VpnProfile vpnProfile;
    private String conf;
    private Region region = null;

    private OpenVPNService service = null;

    @NonNull
    public static synchronized CypherpunkVPN getInstance() {
        if (singleton == null)
            singleton = new CypherpunkVPN();
        return singleton;
    }

    public static boolean protectSocket(Socket s) {
        if (getInstance().service == null)
            return false;

        return getInstance().service.protect(s);
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) iBinder;
            service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    public void toggle(final Context context, final Context baseContext) {
        CypherpunkVpnStatus status = CypherpunkVpnStatus.getInstance();

        if (status.isDisconnected()) {
            start(context, baseContext);
        }
        if (status.isConnected()) {
            stop();
        }
        if (!status.isConnected() && !status.isDisconnected()) {
            // connecting
            stop();
        }
    }

    public void start(final Context context, final Context baseContext) {
        Timber.d("start()");

        CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
        Intent serviceIntent = new Intent(baseContext, OpenVPNService.class);
        serviceIntent.setAction(OpenVPNService.START_SERVICE);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        try {
            conf = generateConfig(context);
            if (conf == null) {
                Timber.d("Unable to generate OpenVPN profile!");
                return;
            }
            cp = new ConfigParser();
            cp.parseConfig(new StringReader(conf));
            vpnProfile = cp.convertProfile();
            ProfileManager.setTemporaryProfile(vpnProfile);
            vpnProfile.mName = region.getRegionName() + ", " + region.getCountry();
            for (String pkg : cypherpunkSetting.disableAppPackageName.split(",")) {
                vpnProfile.mAllowedAppsVpn.add(pkg);
            }
        } catch (Exception e) {
            Timber.e("Exception while generating OpenVPN profile");
            Timber.e(e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                Timber.d("new Thread() -> VPNLaunchHelper()");
                VPNLaunchHelper.startOpenVpn(vpnProfile, baseContext);
            }
        }.start();
    }

    public void stop() {
        Timber.d("stop()");
        if (service != null) {
            OpenVPNManagement manager = service.getManagement();
            if (manager == null)
                return;

            manager.stopVPN(false);
            // privacy firewall killswitch
            CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
            final CypherpunkSetting.InternetKillSwitch internetKillSwitch = cypherpunkSetting.internetKillSwitch();
            switch (internetKillSwitch) {
                case AUTOMATIC:
                    service.stopKillSwitch();
                    break;
                case ALWAYS_ON:
                    break;
                case OFF:
                    service.stopKillSwitch();
                    break;
            }
        }
    }

    private String generateConfig(Context context) {
        Timber.d("generateConfig()");

        List<String> list = new ArrayList<String>();

        // get user prefs
        CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();

        // get currently selected location
        Realm realm = null;
        try {
            realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();
            region = realm.where(Region.class).equalTo("id", cypherpunkSetting.regionId).findFirst();
        } catch (Exception e) {
            Timber.e("Exception while getting Location");
            Timber.e(e);
            if (realm != null)
                realm.close();
            return null;
        }

        // debug print
        /*
        log("vpnCryptoProfile: "+ cypherpunkSetting.vpnCryptoProfile);
        log("privacyFirewallMode: "+ cypherpunkSetting.privacyFirewallMode);
        log("privacyFirewallExemptLAN: "+ cypherpunkSetting.privacyFirewallExemptLAN);
        log("vpnBackend: "+ cypherpunkSetting.vpnBackend);
        log("vpnPortLocal: "+ cypherpunkSetting.vpnPortLocal);
        log("vpnPortRemote: "+ cypherpunkSetting.vpnPortRemote);
        log("disableAppPackageName: "+ cypherpunkSetting.disableAppPackageName);
        */

        // standard options
        list.add("client");
        list.add("dev tun");
        list.add("tls-client");
        list.add("resolv-retry infinite");
        list.add("route-delay 0");

        // mtu settings
        // "it's best to fix the tunnel MTU at 1500 and vary the other parameters (and use --mssfix to prevent fragmentation rather than a lower tunnel MTU)."
        list.add("tun-mtu 1500");
        //list.add("link-mtu 1280");
        list.add("mssfix 1280");

        // security/privacy options
        list.add("tls-version-min 1.2");
        list.add("remote-cert-eku \"TLS Web Server Authentication\"");
        list.add("verify-x509-name " + region.getOvHostname() + " name");
        list.add("tls-cipher TLS-DHE-RSA-WITH-AES-256-GCM-SHA384:TLS-DHE-RSA-WITH-AES-256-CBC-SHA256:TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA256");
        list.add("auth SHA256");

        // ipv6 kill
        list.add("ifconfig-ipv6 fd25::1/64 ::1");
        list.add("route-ipv6 ::/0 ::1");

        // disable ncp
        list.add("ncp-disable");

        // vpn protocol + remote port
        final CypherpunkSetting.RemotePort remotePort = cypherpunkSetting.remotePort();
        list.add("proto " + remotePort.category.name().toLowerCase());
        int rport = remotePort.port.value();

        // vpn crypto profile
        if (cypherpunkSetting.vpnCryptoProfile != null && cypherpunkSetting.vpnCryptoProfile.length() > 0) {
            switch (cypherpunkSetting.vpnCryptoProfile) {
                case "setting_vpn_crypto_profile_none":
                    list.add("remote " + region.getOvNone() + " " + rport);
                    list.add("cipher none");
                    break;
                case "setting_vpn_crypto_profile_default":
                    list.add("remote " + region.getOvDefault() + " " + rport);
                    list.add("cipher AES-128-GCM");
                    break;
                case "setting_vpn_crypto_profile_strong":
                    list.add("remote " + region.getOvStrong() + " " + rport);
                    list.add("cipher AES-256-GCM");
                    break;
                case "setting_vpn_crypto_profile_stealth":
                    list.add("remote " + region.getOvStealth() + " " + rport);
                    list.add("cipher AES-128-GCM");
                    list.add("scramble obfuscate cypherpunk-xor-key"); // requires xorpatch'd openvpn
                    break;
            }
        }

        // local port
        int lport = 0;
        /*
        if (cypherpunkSetting.vpnPortLocal != null && cypherpunkSetting.vpnPortLocal.length() > 0)
            lport = Integer.parseInt(cypherpunkSetting.vpnPortLocal);
        */
        if (lport > 0 && lport < 65535) {
            list.add("lport " + lport);
            list.add("bind");
        } else {
            // need "nobind" to bind to a random port
            list.add("nobind");
        }

        // privacy firewall killswitch
        final CypherpunkSetting.InternetKillSwitch internetKillSwitch = cypherpunkSetting.internetKillSwitch();
        switch (internetKillSwitch) {
            case AUTOMATIC:
            case ALWAYS_ON:
                list.add("persist-tun");
                break;
            case OFF:
                break;
        }

        // privacy firewall exempt LAN from killswitch
        if (cypherpunkSetting.privacyFirewallExemptLAN)
            list.add("redirect-gateway autolocal unblock-local");
        else
            list.add("redirect-gateway autolocal block-local");

        // build DNS settings according to cypherplay/blocker settings
        int dns = 10;
        if (cypherpunkSetting.vpnDnsBlockAds) dns += 1;
        if (cypherpunkSetting.vpnDnsBlockMalware) dns += 2;
        if (cypherpunkSetting.vpnDnsCypherplay) dns += 4;
        list.add("pull-filter ignore \"dhcp-option DNS 10.10.10.10\"");
        list.add("pull-filter ignore \"route 10.10.10.10 255.255.255.255\"");
        list.add("dhcp-option DNS 10.10.10." + dns);
        list.add("route 10.10.10." + dns);

        // append username/password
        /*
        log("password is "+ UserManager.getPassword());
        log("username is "+ UserManager.getMailAddress());
        */
        list.add("<auth-user-pass>");
        list.add(UserManager.getVpnUsername());
        list.add(UserManager.getVpnPassword());
        list.add("</auth-user-pass>");

        // append contents of openvpn.conf
        try {
            InputStream is = context.getAssets().open("openvpn.conf");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null)
                list.add(line);
        } catch (Exception e) {
            Timber.e("unable to read openvpn.conf: " + e.toString());
            return null;
        }

        String[] confLines = list.toArray(new String[0]);
        String conf = TextUtils.join("\n", confLines);

        // debug print
        //for (String line : confLines) log(line);

        return conf;
    }
}
