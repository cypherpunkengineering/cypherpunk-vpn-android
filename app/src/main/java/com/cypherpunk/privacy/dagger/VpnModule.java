package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.vpn.VpnManager;
import com.cypherpunk.privacy.vpn.VpnStatusHolder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class VpnModule {

    @Provides
    @Singleton
    public VpnStatusHolder provideVpnStatusHolder(VpnSetting vpnSetting,
                                                  VpnServerRepository vpnServerRepository) {
        return new VpnStatusHolder(vpnSetting, vpnServerRepository);
    }

    @Provides
    @Singleton
    public VpnManager provideCypherpunkVPN(VpnSetting vpnSetting, AccountSetting accountSetting) {
        return new VpnManager(vpnSetting, accountSetting);
    }
}
