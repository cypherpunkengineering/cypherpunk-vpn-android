package com.cypherpunk.android.vpn;

import android.app.Application;

import com.cypherpunk.android.vpn.dagger.AppComponent;
import com.cypherpunk.android.vpn.dagger.DaggerAppComponent;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.UserSetting;
import com.deploygate.sdk.DeployGate;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.os.operando.garum.Configuration;
import com.os.operando.garum.Garum;

import de.blinkt.openvpn.core.PRNGFixes;
import de.blinkt.openvpn.core.VpnStatus;

public class CypherpunkApplication extends Application {

    public static CypherpunkApplication instance;

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PRNGFixes.apply();
        //VpnStatus.initLogCache(getApplicationContext().getCacheDir());

        appComponent = DaggerAppComponent.create();

        Configuration.Builder builder = new Configuration.Builder(getApplicationContext());
        //noinspection unchecked
        builder.setModelClasses(CypherpunkSetting.class, UserSetting.class);
        Garum.initialize(builder.create());
        DeployGate.install(this);
        Hawk.init(this)
                .setStorage(HawkBuilder.newSharedPrefStorage(this))
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
