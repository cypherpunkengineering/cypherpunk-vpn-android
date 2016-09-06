package com.cypherpunk.android.vpn;

import android.app.Application;

import com.cypherpunk.android.vpn.dagger.AppComponent;
import com.cypherpunk.android.vpn.dagger.DaggerAppComponent;
import com.deploygate.sdk.DeployGate;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
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
        VpnStatus.initLogCache(getApplicationContext().getCacheDir());

        appComponent = DaggerAppComponent.create();

        Garum.initialize(this);
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
