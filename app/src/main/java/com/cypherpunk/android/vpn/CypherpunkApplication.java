package com.cypherpunk.android.vpn;

import android.app.Application;

import com.deploygate.sdk.DeployGate;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

public class CypherpunkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DeployGate.install(this);
        Hawk.init(this)
                .setStorage(HawkBuilder.newSharedPrefStorage(this))
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
                .build();
    }
}
