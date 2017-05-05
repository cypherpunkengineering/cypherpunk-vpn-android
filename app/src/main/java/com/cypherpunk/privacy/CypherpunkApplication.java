package com.cypherpunk.privacy;

import android.app.Application;
import android.support.annotation.VisibleForTesting;

import com.cypherpunk.privacy.dagger.AppComponent;
import com.cypherpunk.privacy.dagger.DaggerAppComponent;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.UserSettingPref;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.os.operando.garum.Configuration;
import com.os.operando.garum.Garum;

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
        appComponent = DaggerAppComponent.create();

        Configuration.Builder builder = new Configuration.Builder(getApplicationContext());
        //noinspection unchecked
        builder.setModelClasses(CypherpunkSetting.class, UserSettingPref.class);
        Garum.initialize(builder.create());

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
