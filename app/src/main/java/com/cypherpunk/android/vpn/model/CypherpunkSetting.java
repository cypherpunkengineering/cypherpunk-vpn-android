package com.cypherpunk.android.vpn.model;

import com.os.operando.garum.annotations.DefaultInt;
import com.os.operando.garum.annotations.DefaultString;
import com.os.operando.garum.annotations.Pref;
import com.os.operando.garum.annotations.PrefKey;
import com.os.operando.garum.models.PrefModel;


@Pref(name = "cypherpunk_setting")
public class CypherpunkSetting extends PrefModel {

    @PrefKey
    @DefaultString("Automatic")
    public String protocol;

    @PrefKey("local_port")
    @DefaultInt(8888)
    public int localPort;

    @PrefKey("remote_port")
    @DefaultString("Automatic")
    public String remotePort;

    @PrefKey
    @DefaultString("Automatic")
    public String firewall;

    @PrefKey("encryption_level")
    @DefaultString("Automatic")
    public String encryptionLevel;

    @PrefKey
    @DefaultString("AEC 728")
    public String cipher;

    @PrefKey
    @DefaultString("SHA 1")
    public String authentication;

    @PrefKey
    @DefaultString("RSA 2048")
    public String key;
}
