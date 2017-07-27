package com.cypherpunk.privacy.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.datasource.vpn.InternetKillSwitch;
import com.cypherpunk.privacy.datasource.vpn.RemotePort;
import com.cypherpunk.privacy.datasource.vpn.TunnelMode;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;

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
import timber.log.Timber;

/**
 * Created by jmaurice on 7/12/16.
 */
public class CypherpunkVPN {
    private static CypherpunkVPN singleton;

    private ConfigParser cp;
    private VpnProfile vpnProfile;
    private String conf;
    private VpnServer vpnServer;

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

    public VpnServer getVpnServer() {
        return vpnServer;
    }

    public void setVpnServer(VpnServer vpnServer) {
        this.vpnServer = vpnServer;
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

    public void toggle(final Context context, final Context baseContext,
                       VpnSetting vpnSetting, AccountSetting accountSetting,
                       VpnServerRepository vpnServerRepository) {
        CypherpunkVpnStatus status = CypherpunkVpnStatus.getInstance();

        if (status.isDisconnected()) {
            start(context, baseContext, vpnSetting, accountSetting, vpnServerRepository);
        }
        if (status.isConnected()) {
            stop(vpnSetting);
        }
        if (!status.isConnected() && !status.isDisconnected()) {
            // connecting
            stop(vpnSetting);
        }
    }

    public void start(final Context context, final Context baseContext,
                      VpnSetting vpnSetting, AccountSetting accountSetting,
                      VpnServerRepository vpnServerRepository) {
        Timber.d("start()");

        Intent serviceIntent = new Intent(baseContext, OpenVPNService.class);
        serviceIntent.setAction(OpenVPNService.START_SERVICE);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        try {
            conf = generateConfig(context, vpnSetting, accountSetting, vpnServerRepository);
            if (conf == null) {
                Timber.d("Unable to generate OpenVPN profile!");
                return;
            }
            cp = new ConfigParser();
            cp.parseConfig(new StringReader(conf));
            vpnProfile = cp.convertProfile();
            ProfileManager.setTemporaryProfile(vpnProfile);
            vpnProfile.mName = vpnServer.name() + ", " + vpnServer.country();
            for (String pkg : vpnSetting.exceptAppList()) {
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

    public void stop(VpnSetting vpnSetting) {
        Timber.d("stop()");
        if (service != null) {
            OpenVPNManagement manager = service.getManagement();
            if (manager == null)
                return;

            manager.stopVPN(false);
            // privacy firewall killswitch
            switch (vpnSetting.internetKillSwitch()) {
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

    @Nullable
    private String generateConfig(Context context, VpnSetting vpnSetting,
                                  AccountSetting accountSetting, VpnServerRepository vpnServerRepository) {
        Timber.d("generateConfig()");

        List<String> list = new ArrayList<String>();

        // get currently selected location
        final VpnServer vpnServer = vpnServerRepository.find(vpnSetting.regionId());
        if (vpnServer == null) {
            return null;
        }

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
        list.add("verify-x509-name " + vpnServer.ovHostname() + " name");
        list.add("tls-cipher TLS-ECDHE-RSA-WITH-AES-256-GCM-SHA384:TLS-ECDHE-RSA-WITH-AES-256-CBC-SHA256:TLS-ECDHE-RSA-WITH-AES-128-GCM-SHA256:TLS-ECDHE-RSA-WITH-AES-128-CBC-SHA256");
        list.add("auth SHA256");

        // ipv6 kill
        list.add("ifconfig-ipv6 fd25::1/64 ::1");
        list.add("route-ipv6 ::/0 ::1");

        // disable ncp
        list.add("ncp-disable");

        // vpn protocol + remote port
        final RemotePort remotePort = vpnSetting.remotePort();
        list.add("proto " + remotePort.type().name().toLowerCase());
        int rport = remotePort.port().value();

        // Always try to send the server a courtesy exit notification in UDP mode
        if (remotePort.type().name().toLowerCase().equals("udp"))
            list.add("explicit-exit-notify");

        // vpn crypto profile
        final TunnelMode tunnelMode = vpnSetting.tunnelMode();
        switch (tunnelMode) {
            case MAX_SPEED:
                list.add("remote " + vpnServer.ovNone() + " " + rport);
                list.add("cipher " + tunnelMode.cipher().value());
                break;
            case RECOMMENDED:
                list.add("remote " + vpnServer.ovDefault() + " " + rport);
                list.add("cipher " + tunnelMode.cipher().value());
                break;
            case MAX_PRIVACY:
                list.add("remote " + vpnServer.ovStrong() + " " + rport);
                list.add("cipher " + tunnelMode.cipher().value());
                break;
            case MAX_STEALTH:
                list.add("remote " + vpnServer.ovStealth() + " " + rport);
                list.add("cipher " + tunnelMode.cipher().value());
                list.add("scramble obfuscate cypherpunk-xor-key"); // requires xorpatch'd openvpn
                break;
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
        final InternetKillSwitch internetKillSwitch = vpnSetting.internetKillSwitch();
        switch (internetKillSwitch) {
            case AUTOMATIC:
            case ALWAYS_ON:
                list.add("persist-tun");
                break;
            case OFF:
                break;
        }

        // privacy firewall exempt LAN from killswitch
        if (vpnSetting.allowLanTraffic()) {
            list.add("redirect-gateway autolocal unblock-local");
        } else {
            list.add("redirect-gateway autolocal block-local");
        }

        // build DNS settings according to cypherplay/blocker settings
        int dns = 10;
        if (vpnSetting.isBlockAds()) {
            dns += 1;
        }
        if (vpnSetting.isBlockMalware()) {
            dns += 2;
        }
        if (vpnSetting.isCypherplayEnabled()) {
            dns += 4;
        }
        list.add("pull-filter ignore \"dhcp-option DOMAIN local\"");
        list.add("pull-filter ignore \"dhcp-option DNS 10.10.10.10\"");
        list.add("pull-filter ignore \"dhcp-option DNS 10.10.11.10\"");
        list.add("pull-filter ignore \"dhcp-option DNS 10.10.12.10\"");
        list.add("pull-filter ignore \"route 10.10.10.10 255.255.255.255\"");
        list.add("pull-filter ignore \"route 10.10.11.10 255.255.255.255\"");
        list.add("pull-filter ignore \"route 10.10.12.10 255.255.255.255\"");
        list.add("dhcp-option DOMAIN local");
        list.add("dhcp-option DNS 10.10.10." + dns);
        list.add("dhcp-option DNS 10.10.11." + dns);
        list.add("dhcp-option DNS 10.10.12." + dns);
        list.add("route 10.10.10." + dns);
        list.add("route 10.10.11." + dns);
        list.add("route 10.10.12." + dns);

        // append username/password
        list.add("<auth-user-pass>");
        list.add(accountSetting.username());
        list.add(accountSetting.password());
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
