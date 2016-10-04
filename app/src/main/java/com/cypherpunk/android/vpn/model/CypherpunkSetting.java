package com.cypherpunk.android.vpn.model;

import com.os.operando.garum.annotations.DefaultBoolean;
import com.os.operando.garum.annotations.DefaultInt;
import com.os.operando.garum.annotations.DefaultString;
import com.os.operando.garum.annotations.Pref;
import com.os.operando.garum.annotations.PrefKey;
import com.os.operando.garum.models.PrefModel;


@Pref(name = "cypherpunk_setting")
public class CypherpunkSetting extends PrefModel {

    @PrefKey("vpn_auto_start_connect")
    @DefaultBoolean(false)
    public boolean vpnAutoStartConnect;

    @PrefKey("privacy_firewall_mode")
    @DefaultString("setting_privacy_firewall_mode_auto")
    public String privacyFirewallMode;

    @PrefKey("privacy_firewall_exempt_lan")
    @DefaultBoolean(true)
    public boolean privacyFirewallExemptLAN;

    @PrefKey("vpn_crypto_profile")
    @DefaultString("setting_vpn_crypto_profile_default")
    public String vpnCryptoProfile;

    @PrefKey("vpn_crypto_profile_cipher")
    @DefaultString("setting_vpn_crypto_cipher_aes256")
    public String vpnCryptoProfileCipher;

    @PrefKey("vpn_crypto_profile_auth")
    @DefaultString("setting_vpn_crypto_auth_sha1")
    public String vpnCryptoProfileAuth;

    @PrefKey("vpn_crypto_profile_keylen")
    @DefaultString("setting_vpn_crypto_keylen_rsa2048")
    public String vpnCryptoProfileKeylen;

    @PrefKey("vpn_protocol")
    @DefaultString("setting_vpn_protocol_openvpn23_udp")
    public String vpnProtocol;

    @PrefKey("vpn_port_remote")
    @DefaultString("")
    public String vpnPortRemote;

    @PrefKey("vpn_port_local")
    @DefaultString("")
    public String vpnPortLocal;

    @PrefKey("vpn_port_forwarding")
    @DefaultBoolean(false)
    public boolean portForwarding;
}
