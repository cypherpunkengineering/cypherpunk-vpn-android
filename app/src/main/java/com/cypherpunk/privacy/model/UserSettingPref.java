package com.cypherpunk.privacy.model;

import com.os.operando.garum.annotations.Pref;
import com.os.operando.garum.annotations.PrefKey;
import com.os.operando.garum.models.PrefModel;


@Pref(name = "user_setting")
public class UserSettingPref extends PrefModel {

    @PrefKey
    public String mail;

    @PrefKey("user_status_type")
    public String userStatusType;

    @PrefKey("user_status_renewal")
    public String userStatusRenewal;

    @PrefKey("user_status_expiration")
    public String userStatusExpiration;
}
