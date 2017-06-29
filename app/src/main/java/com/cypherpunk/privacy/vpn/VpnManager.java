package com.cypherpunk.privacy.vpn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cypherpunk.privacy.datasource.vpn.InternetKillSwitch;
import com.cypherpunk.privacy.datasource.vpn.RemotePort;
import com.cypherpunk.privacy.datasource.vpn.TunnelMode;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;

import java.io.BufferedReader;
import java.io.IOException;
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

public class VpnManager {

    @NonNull
    private final VpnSetting vpnSetting;
    @NonNull
    private final AccountSetting accountSetting;

    @Nullable
    private OpenVPNService service = null;

    public VpnManager(@NonNull VpnSetting vpnSetting, @NonNull AccountSetting accountSetting) {
        this.vpnSetting = vpnSetting;
        this.accountSetting = accountSetting;
    }

    boolean protectSocket(@NonNull Socket socket) {
        return service != null && service.protect(socket);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            final OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) iBinder;
            service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    public void start(@NonNull Context ctx, @NonNull VpnServer vpnServer) {
        Timber.d("start()");

        final Context context = ctx.getApplicationContext();

        final Intent intent = new Intent(context, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);

        final String config = generateConfig(context, vpnServer);
        if (config == null) {
            Timber.d("Unable to generate OpenVPN profile!");
            return;
        }

        try {
            final ConfigParser cp = new ConfigParser();
            cp.parseConfig(new StringReader(config));

            final VpnProfile vpnProfile = cp.convertProfile();
            ProfileManager.setTemporaryProfile(vpnProfile);
            vpnProfile.mName = vpnServer.name() + ", " + vpnServer.country();
            for (String packageName : vpnSetting.exceptAppList()) {
                vpnProfile.mAllowedAppsVpn.add(packageName);
            }

            new Thread() {
                @Override
                public void run() {
                    Timber.d("new Thread() -> VPNLaunchHelper()");
                    VPNLaunchHelper.startOpenVpn(vpnProfile, context);
                }
            }.start();

        } catch (Exception e) {
            Timber.e("Exception while generating OpenVPN profile");
            Timber.e(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        Timber.d("stop");
        if (service != null) {
            final OpenVPNManagement manager = service.getManagement();
            if (manager != null) {
                manager.stopVPN(false);

                // privacy firewall kill switch
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
    }

    @Nullable
    private String generateConfig(@NonNull Context context, @NonNull VpnServer vpnServer) {
        Timber.d("generateConfig()");

        final List<String> list = new ArrayList<>();

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
        final int removePort = remotePort.port().value();

        // Always try to send the server a courtesy exit notification in UDP mode
        if (remotePort.type().name().toLowerCase().equals("udp"))
            list.add("explicit-exit-notify");

        // vpn crypto profile
        final TunnelMode tunnelMode = vpnSetting.tunnelMode();
        switch (tunnelMode) {
            case MAX_SPEED:
                list.add("remote " + vpnServer.ovNone() + " " + removePort);
                list.add("cipher " + tunnelMode.cipher().value());
                break;
            case RECOMMENDED:
                list.add("remote " + vpnServer.ovDefault() + " " + removePort);
                list.add("cipher " + tunnelMode.cipher().value());
                break;
            case MAX_PRIVACY:
                list.add("remote " + vpnServer.ovStrong() + " " + removePort);
                list.add("cipher " + tunnelMode.cipher().value());
                break;
            case MAX_STEALTH:
                list.add("remote " + vpnServer.ovStealth() + " " + removePort);
                list.add("cipher " + tunnelMode.cipher().value());
                list.add("scramble obfuscate cypherpunk-xor-key"); // requires xorpatch'd openvpn
                break;
        }

        // local port
        final int localPort = 0;
//        if (cypherpunkSetting.vpnPortLocal != null && cypherpunkSetting.vpnPortLocal.length() > 0) {
//            localPort = Integer.parseInt(cypherpunkSetting.vpnPortLocal);
//        }
        if (localPort > 0 && localPort < 65535) {
            list.add("lport " + localPort);
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
        BufferedReader br = null;
        try {
            final InputStream is = context.getAssets().open("openvpn.conf");
            br = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s = br.readLine()) != null) {
                list.add(s);
            }
        } catch (IOException e) {
            Timber.e("unable to read openvpn.conf: " + e.toString());
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        final String[] confLines = list.toArray(new String[list.size()]);

        if (BuildConfig.DEBUG) {
            for (String line : confLines) {
                Timber.d(line);
            }
        }

        return TextUtils.join("\n", confLines);
    }
}
