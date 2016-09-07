package com.cypherpunk.android.vpn.model;

import com.os.operando.garum.annotations.DefaultString;
import com.os.operando.garum.annotations.Pref;
import com.os.operando.garum.annotations.PrefKey;
import com.os.operando.garum.models.PrefModel;


@Pref(name = "cypherpunk_setting")
public class CypherpunkSetting extends PrefModel {

    @PrefKey
    @DefaultString("automatic")
    public String protocol;

    @PrefKey("local_port")
    @DefaultString("automatic")
    public String local_port;

    @PrefKey("remote_port")
    public String remotePort;

    @PrefKey
    @DefaultString("automatic")
    public String firewall;

    @PrefKey("encryption_level")
    @DefaultString("automatic")
    public String encryptionLevel;

    @PrefKey
    @DefaultString("aec_728")
    public String cipher;

    @PrefKey
    @DefaultString("sha_1")
    public String authentication;

    @PrefKey
    @DefaultString("rsa_2048")
    public String key;
}
