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

    @PrefKey("firewall")
    @DefaultString("automatic")
    public String firewall;

    @PrefKey("encryption_level")
    @DefaultString("automatic")
    public String encryptionLevel;
}
