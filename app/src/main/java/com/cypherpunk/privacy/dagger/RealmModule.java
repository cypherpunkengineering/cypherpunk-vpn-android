package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.BuildConfig;
import com.cypherpunk.privacy.CypherpunkApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Module
public class RealmModule {

    @Provides
    @Singleton
    public RealmConfiguration provideRealmConfiguration() {
        final RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
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
