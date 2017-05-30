package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.datasource.vpn.RealmVpnServerRepository;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class VpnServerModule {

    @Provides
    @Singleton
    public VpnServerRepository provideVpnServerRepository() {
        return new RealmVpnServerRepository();
    }
}
