package com.cypherpunk.android.vpn;

import android.app.Application;

import com.deploygate.sdk.DeployGate;

public class CypherpunkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DeployGate.install(this);
    }
}
