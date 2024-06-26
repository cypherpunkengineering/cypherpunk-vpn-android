package com.cypherpunk.privacy.dagger;

import android.content.Context;
import android.support.annotation.NonNull;

import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.datasource.account.CypherpunkAccountSetting;
import com.cypherpunk.privacy.datasource.vpn.CypherpunkVpnSetting;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingModule {

    @NonNull
    private final Context context;

    public SettingModule(@NonNull Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    @Provides
    @Singleton
    public VpnSetting provideVpnSetting() {
        return new CypherpunkVpnSetting(context);
    }

    @Provides
    @Singleton
    public AccountSetting provideAccountSetting() {
        return new CypherpunkAccountSetting(context);
    }
}
