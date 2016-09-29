package com.cypherpunk.android.vpn.model;

import com.os.operando.garum.annotations.Pref;
import com.os.operando.garum.annotations.PrefKey;
import com.os.operando.garum.models.PrefModel;


@Pref(name = "user_setting")
public class UserSetting extends PrefModel {

    @PrefKey
    public String mail;
}
