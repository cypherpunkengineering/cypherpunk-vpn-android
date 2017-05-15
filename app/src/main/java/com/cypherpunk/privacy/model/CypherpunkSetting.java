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

    @PrefKey("vpn_port_remote")
    @DefaultString("")
    public String vpnPortRemote;

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
}
