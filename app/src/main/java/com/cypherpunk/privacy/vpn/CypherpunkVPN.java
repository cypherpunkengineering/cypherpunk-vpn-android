package com.cypherpunk.privacy.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.android.annotations.NonNull;
import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.Region;

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
    private Region region = null;

    private OpenVPNService service = null;

    @NonNull
    public static synchronized CypherpunkVPN getInstance()
    {
        if (singleton == null)
            singleton = new CypherpunkVPN();
        return singleton;
    }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

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
            if (conf == null)
            {
                log("Unable to generate OpenVPN profile!");
                return;
            }
            cp = new ConfigParser();
            cp.parseConfig(new StringReader(conf));
            vpnProfile = cp.convertProfile();
            ProfileManager.setTemporaryProfile(vpnProfile);
            vpnProfile.mName = region.getRegionName() + ", " + region.getCountryCode();

            //log("vpn profile check: "+vpnProfile.checkProfile(context));
        }
        catch (Exception e)
        {
            log("Exception while generating OpenVPN profile");
            log(e.getLocalizedMessage());
            e.printStackTrace();
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
        Realm realm = null;
        try
        {
            realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();
            region = realm.where(Region.class).equalTo("id", cypherpunkSetting.regionId).findFirst();
        }
        catch (Exception e)
        {
            log("Exception while getting Location");
            e.printStackTrace();
            if (realm != null)
                realm.close();
            return null;
        }

        // debug print
        /*
        log("vpnCryptoProfile: "+ cypherpunkSetting.vpnCryptoProfile);
        log("vpnCryptoProfileAuth: "+ cypherpunkSetting.vpnCryptoProfileAuth);
        log("vpnCryptoProfileCipher: "+ cypherpunkSetting.vpnCryptoProfileCipher);
        log("vpnCryptoProfileKeylen: "+ cypherpunkSetting.vpnCryptoProfileKeylen);
        log("privacyFirewallMode: "+ cypherpunkSetting.privacyFirewallMode);
        log("privacyFirewallExemptLAN: "+ cypherpunkSetting.privacyFirewallExemptLAN);
        log("vpnBackend: "+ cypherpunkSetting.vpnBackend);
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
        list.add("verify-x509-name " + region.getOvHostname() + " name");

        // vpn protocol + remote port
        String proto = "udp";
        int rport = 7133;
        if (cypherpunkSetting.vpnPortRemote != null && cypherpunkSetting.vpnPortRemote.length() > 0)
        {
            switch (cypherpunkSetting.vpnPortRemote)
            {
                case "UDP/7133":
                    proto = "udp";
                    rport = 7133;
                    break;
                case "UDP/5060":
                    proto = "udp";
                    rport = 5060;
                    break;
                case "UDP/53":
                    proto = "udp";
                    rport = 53;
                    break;
                case "TCP/7133":
                    proto = "tcp";
                    rport = 7133;
                    break;
                case "TCP/443":
                    proto = "tcp";
                    rport = 443;
                    break;
            }
        }

        // vpn crypto profile
        if (cypherpunkSetting.vpnCryptoProfile != null && cypherpunkSetting.vpnCryptoProfile.length() > 0)
        {
            switch (cypherpunkSetting.vpnCryptoProfile)
            {
                case "setting_vpn_crypto_profile_none":
                    list.add("proto " + proto);
                    list.add("remote " + region.getOvNone() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA256");
                    list.add("cipher none");
                    list.add("auth SHA1");
                    break;
                case "setting_vpn_crypto_profile_default":
                    list.add("proto " + proto);
                    list.add("remote " + region.getOvDefault() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA256");
                    list.add("cipher AES-128-CBC");
                    list.add("auth SHA256");
                    break;
                case "setting_vpn_crypto_profile_strong":
                    list.add("proto " + proto);
                    list.add("remote " + region.getOvStrong() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-256-GCM-SHA384:TLS-DHE-RSA-WITH-AES-256-CBC-SHA256");
                    list.add("cipher AES-256-CBC");
                    list.add("auth SHA512");
                    break;
                case "setting_vpn_crypto_profile_stealth":
                    list.add("proto " + proto);
                    list.add("remote " + region.getOvStealth() + " " + rport);
                    list.add("tls-cipher TLS-DHE-RSA-WITH-AES-128-GCM-SHA256:TLS-DHE-RSA-WITH-AES-128-CBC-SHA256");
                    list.add("cipher AES-128-CBC");
                    list.add("auth SHA256");
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
        if (lport > 0 && lport < 65535)
        {
            list.add("lport " + lport);
            list.add("bind");
        }
        else
        {
            // need "nobind" to bind to a random port
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
