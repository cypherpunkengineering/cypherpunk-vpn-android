package com.cypherpunk.android.vpn.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.android.annotations.NonNull;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.Location;
import com.cypherpunk.android.vpn.ui.main.CypherpunkLaunchVPN;
import com.cypherpunk.android.vpn.ui.main.MainActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import io.realm.Realm;

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
    private Location location = null;

    private OpenVPNService service = null;

    @NonNull
    public static synchronized CypherpunkVPN getInstance()
    {
        if (singleton == null)
            singleton = new CypherpunkVPN();
        return singleton;
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) iBinder;
            service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            service = null;
        }
    };

    private void log(String str) {
        Log.w("CypherpunkVPN", str);
    }

    public void toggle(final Context context, final Context baseContext)
    {
        CypherpunkVpnStatus status = CypherpunkVpnStatus.getInstance();

        if (status.isDisconnected()) {
            start(context, baseContext);
        }
        if (status.isConnected()) {
            stop(context, baseContext);
        }
        if (!status.isConnected() && !status.isDisconnected()) {
            // connecting
            stop(context, baseContext);
        }
    }

    public void start(final Context context, final Context baseContext)
    {
        log("start()");

        CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
        Intent serviceIntent = new Intent(baseContext, OpenVPNService.class);
        serviceIntent.setAction(OpenVPNService.START_SERVICE);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        try
        {
            conf = generateConfig(context);
            cp = new ConfigParser();
            cp.parseConfig(new StringReader(conf));
            vpnProfile = cp.convertProfile();
            ProfileManager.setTemporaryProfile(vpnProfile);
            vpnProfile.mName = location.getRegionName() + ", " + location.getCountryCode();

            //log("vpn profile check: "+vpnProfile.checkProfile(context));
        }
        catch (Exception e)
        {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                log("new Thread() -> VPNLaunchHelper()");
                VPNLaunchHelper.startOpenVpn(vpnProfile, baseContext);
            }
        }.start();
    }

    public void stop(final Context context, final Context baseContext)
    {
        log("stop()");
        if (service != null)
        {
            service.getManagement().stopVPN(false);
            // privacy firewall killswitch
            CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
            if (cypherpunkSetting.privacyFirewallMode != null && cypherpunkSetting.privacyFirewallMode.length() > 0)
            {
                switch (cypherpunkSetting.privacyFirewallMode)
                {
                    case "setting_privacy_firewall_mode_auto":
                        service.stopKillSwitch();
                        break;
                    case "setting_privacy_firewall_mode_always":
                        break;
                    case "setting_privacy_firewall_mode_never":
                        service.stopKillSwitch();
                        break;
                }
            }
        }
    }

    private String generateConfig(Context context)
    {
        log("generateConfig()");

        List<String> list = new ArrayList<String>();

        // get user prefs
        CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();

        // get currently selected location
        Realm realm = Realm.getDefaultInstance();
        location = realm.where(Location.class).equalTo("selected", true).findFirst();
        realm.close();

        // debug print
        /*
        log("vpnCryptoProfile: "+ cypherpunkSetting.vpnCryptoProfile);
        log("vpnCryptoProfileAuth: "+ cypherpunkSetting.vpnCryptoProfileAuth);
        log("vpnCryptoProfileCipher: "+ cypherpunkSetting.vpnCryptoProfileCipher);
        log("vpnCryptoProfileKeylen: "+ cypherpunkSetting.vpnCryptoProfileKeylen);
        log("privacyFirewallMode: "+ cypherpunkSetting.privacyFirewallMode);
        log("privacyFirewallExemptLAN: "+ cypherpunkSetting.privacyFirewallExemptLAN);
        log("vpnProtocol: "+ cypherpunkSetting.vpnProtocol);
        log("vpnPortLocal: "+ cypherpunkSetting.vpnPortLocal);
        log("vpnPortRemote: "+ cypherpunkSetting.vpnPortRemote);
        */

        // standard options
        list.add("client");
        list.add("dev tun");
        list.add("tls-client");
        list.add("resolv-retry infinite");
        list.add("route-delay 0");

        // security/privacy options
        list.add("tls-version-min 1.2");
        list.add("remote-cert-eku \"TLS Web Server Authentication\"");
        list.add("verify-x509-name " + location.getOvHostname() + " name");

        // vpn protocol
        String proto = "udp";
        if (cypherpunkSetting.vpnProtocol != null && cypherpunkSetting.vpnProtocol.length() > 0)
        {
            switch (cypherpunkSetting.vpnProtocol)
            {
                case "setting_vpn_protocol_openvpn23_udp":
                case "setting_vpn_protocol_openvpn31_udp":
                    proto = "udp";
                    break;
                case "setting_vpn_protocol_openvpn23_tcp":
                case "setting_vpn_protocol_openvpn31_tcp":
                    proto = "tcp";
                    break;
            }
        }
        list.add("proto " + proto);

        // remote port
        int rport = 7133;
        if (cypherpunkSetting.vpnPortRemote != null && cypherpunkSetting.vpnPortRemote.length() > 0)
            rport = Integer.parseInt(cypherpunkSetting.vpnPortRemote);
        if (rport < 1 && rport > 65535)
            rport = 7133;

        // vpn crypto profile
        if (cypherpunkSetting.vpnCryptoProfile != null && cypherpunkSetting.vpnCryptoProfile.length() > 0)
        {
            switch (cypherpunkSetting.vpnCryptoProfile)
            {
                case "setting_vpn_crypto_profile_none":
                    list.add("remote " + location.getOvNone() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA256");
                    list.add("cipher none");
                    list.add("auth SHA1");
                    break;
                case "setting_vpn_crypto_profile_strong":
                    list.add("remote " + location.getOvStrong() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-256-GCM-SHA384:TLS-DHE-RSA-WITH-AES-256-CBC-SHA256");
                    list.add("cipher AES-256-CBC");
                    list.add("auth SHA512");
                    break;
                case "setting_vpn_crypto_profile_stealth":
                    list.add("remote " + location.getOvStealth() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA256");
                    list.add("cipher AES-128-CBC");
                    list.add("auth SHA256");
                    // requires xorpatch'd openvpn
                    list.add("scramble obfuscate cypherpunk-xor-key");
                    break;
                case "setting_vpn_crypto_profile_default":
                default:
                    list.add("remote " + location.getOvDefault() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA256");
                    list.add("cipher AES-128-CBC");
                    list.add("auth SHA256");
                    break;
            }
        }

        // local port
        int lport = 0;
        if (cypherpunkSetting.vpnPortLocal != null && cypherpunkSetting.vpnPortLocal.length() > 0)
            lport = Integer.parseInt(cypherpunkSetting.vpnPortLocal);
        if (lport > 0 && lport < 65535)
        {
            list.add("lport " + lport);
            list.add("bind");
        }
        else
        {
            list.add("nobind");
        }

        // privacy firewall killswitch
        if (cypherpunkSetting.privacyFirewallMode != null && cypherpunkSetting.privacyFirewallMode.length() > 0)
        {
            switch (cypherpunkSetting.privacyFirewallMode)
            {
                case "setting_privacy_firewall_mode_auto":
                case "setting_privacy_firewall_mode_always":
                    list.add("persist-tun");
                    break;
                case "setting_privacy_firewall_mode_never":
                    break;
            }
        }

        // privacy firewall exempt LAN from killswitch
        if (cypherpunkSetting.privacyFirewallExemptLAN)
            list.add("redirect-gateway autolocal unblock-local");
        else
            list.add("redirect-gateway autolocal block-local");

        // append username/password
        /*
        log("password is "+ UserManager.getPassword());
        log("username is "+ UserManager.getMailAddress());
        */
        list.add("<auth-user-pass>");
        list.add(UserManager.getMailAddress());
        list.add(UserManager.getPassword());
        list.add("</auth-user-pass>");

        // append contents of openvpn.conf
        try
        {
            InputStream is = context.getAssets().open("openvpn.conf");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null)
                list.add(line);
        }
        catch (Exception e)
        {
            log("unable to read openvpn.conf: " + e.toString());
            return null;
        }

        String[] confLines = list.toArray(new String[0]);
        String conf = TextUtils.join("\n", confLines);

        // debug print
        // for (String line : confLines) log(line);

        return conf;
    }
}
