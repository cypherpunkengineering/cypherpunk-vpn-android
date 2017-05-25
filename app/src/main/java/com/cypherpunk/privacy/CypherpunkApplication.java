package com.cypherpunk.privacy;

import android.app.Application;
import android.support.annotation.VisibleForTesting;

import com.cypherpunk.privacy.dagger.AppComponent;
import com.cypherpunk.privacy.dagger.ClientModule;
import com.cypherpunk.privacy.dagger.DaggerAppComponent;
import com.cypherpunk.privacy.dagger.RealmModule;
import com.cypherpunk.privacy.dagger.SettingModule;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

import de.blinkt.openvpn.core.PRNGFixes;
import io.realm.Realm;

public class CypherpunkApplication extends Application {

    public static CypherpunkApplication instance;

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PRNGFixes.apply();
        //VpnStatus.initLogCache(getApplicationContext().getCacheDir());

        Realm.init(this);
        appComponent = DaggerAppComponent.builder()
                .clientModule(new ClientModule())
                .realmModule(new RealmModule())
                .settingModule(new SettingModule(this))
                .build();

        //DeployGate.install(this);

        Hawk.init(this)
                .setStorage(HawkBuilder.newSharedPrefStorage(this))
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @VisibleForTesting
    public void setAppComponent(AppComponent component) {
        this.appComponent = component;
    }
}
