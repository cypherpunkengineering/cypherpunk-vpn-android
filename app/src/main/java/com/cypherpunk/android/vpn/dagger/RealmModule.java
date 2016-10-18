package com.cypherpunk.android.vpn.dagger;

import com.cypherpunk.android.vpn.BuildConfig;
import com.cypherpunk.android.vpn.CypherpunkApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Module
public class RealmModule {
    @Provides
    CypherpunkApplication provideApplication() {
        return CypherpunkApplication.instance;
    }

    @Provides
    @Singleton
    public RealmConfiguration provideRealmConfiguration(CypherpunkApplication app) {
        final RealmConfiguration.Builder builder = new RealmConfiguration.Builder(app);
        if (BuildConfig.DEBUG) {
            builder.deleteRealmIfMigrationNeeded();
        }
        return builder.build();
    }

    @Provides
    public Realm provideRealm(RealmConfiguration conf) {
        return Realm.getInstance(conf);
    }
}
