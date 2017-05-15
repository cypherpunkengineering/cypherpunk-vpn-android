package com.cypherpunk.privacy.model;

import android.support.annotation.NonNull;

import com.os.operando.garum.annotations.DefaultBoolean;
import com.os.operando.garum.annotations.DefaultString;
import com.os.operando.garum.annotations.Pref;
import com.os.operando.garum.annotations.PrefKey;
import com.os.operando.garum.models.PrefModel;


@Pref(name = "cypherpunk_setting")
public class CypherpunkSetting extends PrefModel {

    @PrefKey("vpn_auto_start_connect")
    @DefaultBoolean(false)
    public boolean vpnAutoStartConnect;

    @PrefKey("vpn_dns_block_ads")
    @DefaultBoolean(false)
    public boolean vpnDnsBlockAds;

    @PrefKey("vpn_dns_block_malware")
    @DefaultBoolean(false)
    public boolean vpnDnsBlockMalware;

    @PrefKey("vpn_dns_cypherplay")
    @DefaultBoolean(false)
    public boolean vpnDnsCypherplay;

    @PrefKey("privacy_firewall_mode")
    @DefaultString("setting_privacy_firewall_mode_never")
    public String internetKillSwitchValue;

    @PrefKey("privacy_firewall_exempt_lan")
    @DefaultBoolean(true)
    public boolean privacyFirewallExemptLAN;

    @PrefKey("vpn_crypto_profile")
    @DefaultString("setting_vpn_crypto_profile_default")
    public String vpnCryptoProfile;

    @PrefKey("vpn_backend")
    @DefaultString("setting_vpn_backend_openvpn23")
    public String vpnBackend;

    @PrefKey("vpn_remote_port_category")
    @DefaultString("")
    public String vpnRemotePortCategory;

    @PrefKey("vpn_remote_port_port")
    public int vpnRemotePortPort;

    @PrefKey("vpn_port_local")
    @DefaultString("")
    public String vpnPortLocal;

    @PrefKey("vpn_port_forwarding")
    @DefaultBoolean(false)
    public boolean portForwarding;

    @PrefKey("vpn_network_auto_secure_untrusted")
    @DefaultBoolean(false)
    public boolean autoSecureUntrusted;

    @PrefKey("vpn_network_auto_secure_other")
    @DefaultBoolean(false)
    public boolean autoSecureOther;

    @PrefKey("vpn_disable_package_name")
    @DefaultString("")
    public String disableAppPackageName;

    @PrefKey("cpn_region_id")
    @DefaultString("")
    public String regionId;

    @PrefKey("analytics")
    @DefaultBoolean(false)
    public boolean analytics;

    //
    // InternetKillSwitch
    //

    @NonNull
    public InternetKillSwitch internetKillSwitch() {
        return InternetKillSwitch.find(internetKillSwitchValue);
    }

    public void updateInternetKillSwitch(@NonNull InternetKillSwitch internetKillSwitch) {
        internetKillSwitchValue = internetKillSwitch.value;
        save();
    }

    public enum InternetKillSwitch {
        AUTOMATIC("setting_privacy_firewall_mode_auto"),
        OFF("setting_privacy_firewall_mode_never"),
        ALWAYS_ON("setting_privacy_firewall_mode_always");

        @NonNull
        private final String value;

        InternetKillSwitch(@NonNull String value) {
            this.value = value;
        }

        @NonNull
        public static InternetKillSwitch find(String value) {
            for (InternetKillSwitch internetKillSwitch : values()) {
                if (internetKillSwitch.value.equals(value)) {
                    return internetKillSwitch;
                }
            }
            return OFF;
        }
    }

    //
    // TunnelMode
    //

    @NonNull
    public TunnelMode tunnelMode() {
        return TunnelMode.find(vpnCryptoProfile);
    }

    public void updateTunnelMode(@NonNull TunnelMode tunnelMode) {
        vpnCryptoProfile = tunnelMode.name();
        save();
    }

    public enum TunnelMode {
        RECOMMENDED(TunnelModeCipher.AES_128_GCM, TunnelModeAuth.SHA_256, TunnelModeKey.RSA_4096),
        MAX_SPEED(TunnelModeCipher.NONE, TunnelModeAuth.SHA_256, TunnelModeKey.RSA_4096),
        MAX_PRIVACY(TunnelModeCipher.AES_256_GCM, TunnelModeAuth.SHA_256, TunnelModeKey.RSA_4096),
        MAX_STEALTH(TunnelModeCipher.AES_128_GCM, TunnelModeAuth.SHA_256, TunnelModeKey.RSA_4096);

        @NonNull
        private final TunnelModeCipher cipher;
        @NonNull
        private final TunnelModeAuth auth;
        @NonNull
        private final TunnelModeKey key;

        TunnelMode(@NonNull TunnelModeCipher cipher, @NonNull TunnelModeAuth auth, @NonNull TunnelModeKey key) {
            this.cipher = cipher;
            this.auth = auth;
            this.key = key;
        }

        @NonNull
        public TunnelModeCipher cipher() {
            return cipher;
        }

        @NonNull
        public TunnelModeAuth auth() {
            return auth;
        }

        @NonNull
        public TunnelModeKey key() {
            return key;
        }

        @NonNull
        public static TunnelMode find(String name) {
            for (TunnelMode tunnelMode : values()) {
                if (tunnelMode.name().equals(name)) {
                    return tunnelMode;
                }
            }
            return RECOMMENDED;
        }
    }

    public enum TunnelModeCipher {
        NONE("NONE"),
        AES_128_GCM("AES-128-GCM"),
        AES_256_GCM("AES-256-GCM");

        @NonNull
        private final String value;

        TunnelModeCipher(@NonNull String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum TunnelModeAuth {
        SHA_256("SHA-256");

        @NonNull
        private final String value;

        TunnelModeAuth(@NonNull String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum TunnelModeKey {
        RSA_4096("RSA-4096");

        @NonNull
        private final String value;

        TunnelModeKey(@NonNull String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

    }

    //
    // RemotePort
    //

    @NonNull
    public RemotePort remotePort() {
        final RemotePortCategory category = RemotePortCategory.find(vpnRemotePortCategory);
        final RemotePortPort port = RemotePortPort.find(vpnRemotePortPort);
        return new RemotePort(category, port);
    }

    public void updateRemotePort(@NonNull RemotePort remotePort) {
        vpnRemotePortCategory = remotePort.category.name();
        vpnRemotePortPort = remotePort.port.value;
        save();
    }

    public static class RemotePort {
        public final RemotePortCategory category;
        public final RemotePortPort port;

        public RemotePort(RemotePortCategory category, RemotePortPort port) {
            this.category = category;
            this.port = port;
        }
    }

    public enum RemotePortCategory {
        UDP,
        TCP;

        @NonNull
        public static RemotePortCategory find(String value) {
            for (RemotePortCategory category : values()) {
                if (category.name().equals(value)) {
                    return category;
                }
            }
            return UDP;
        }
    }

    public enum RemotePortPort {
        PORT_7133(7133),
        PORT_5060(5060),
        PORT_53(53);

        private final int value;

        RemotePortPort(int value) {
            this.value = value;
        }

        @NonNull
        public static RemotePortPort find(int value) {
            for (RemotePortPort port : values()) {
                if (port.value == value) {
                    return port;
                }
            }
            return PORT_7133;
        }

        public int value() {
            return value;
        }
    }
}
