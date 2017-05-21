package com.cypherpunk.privacy.ui.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.vpn.CypherpunkVPN;
import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by jmaurice on 2016/10/07.
 */
public class CypherpunkLaunchVPN extends Activity {
    public static final String AUTO_START = "com.cypherpunk.privacy.AUTO_START";
    public static final String TILE_CLICK = "com.cypherpunk.privacy.TILE_CLICK";
    public static final String NETWORK_TRUSTED = "com.cypherpunk.privacy.NETWORK_TRUSTED";
    public static final String NETWORK_UNTRUSTED = "com.cypherpunk.privacy.NETWORK_UNTRUSTED";

    private static final int START_VPN_PROFILE = 70;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate()");
        super.onCreate(savedInstanceState);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        Intent intent = getIntent();
        handleIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent()");
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // crash fix for invisible dialog, in case perm dialog needs to be shown
        setVisible(true);
    }

    private void handleIntent(Intent intent) {
        Timber.d("handleIntent()");

        // check if user is signed in
        if (!accountSetting.isSignedIn() || intent == null) {
            Timber.d("user not logged in, ignoring intent");
            setIntent(null);
            finish();
            return;
        }

        // called from CypherpunkBootReceiver
        if (intent.getBooleanExtra(AUTO_START, false)) {
            // immediately exit unless user setting for auto start is enabled
            if (!vpnSetting.isAutoConnect()) {
                finish();
                return;
            }

            // prepare vpn service and wait for callback
            Timber.d("auto starting VPN");
            prepareVpnService();
        } else if (intent.getBooleanExtra(TILE_CLICK, false)) {
            // get vpn status
            CypherpunkVpnStatus status = CypherpunkVpnStatus.getInstance();

            // either connecting or already connected, stop vpn and finish activity
            if (!status.isDisconnected()) {
                CypherpunkVPN.getInstance().stop(vpnSetting);
                finish();
                return;
            }

            // prepare vpn service, wait for callback, then connect
            prepareVpnService();
        } else if (intent.getBooleanExtra(NETWORK_TRUSTED, false)) {
            CypherpunkVPN.getInstance().stop(vpnSetting);
            finish();
        } else if (intent.getBooleanExtra(NETWORK_UNTRUSTED, false)) {
            prepareVpnService();
        }

        // done
        //setIntent(null);
        //moveTaskToBack(true);
    }

    private void prepareVpnService() {
        Timber.d("VpnService.prepare()");

        // returns intent if permission dialog is needed
        Intent permissionIntent = VpnService.prepare(this);

        // no dialog needed, start vpn immediately
        if (permissionIntent == null) {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        } else // dialog needed, start intent and wait for callback
        {
            try {
                startActivityForResult(permissionIntent, START_VPN_PROFILE);
            } catch (ActivityNotFoundException e) // buggy sony devices
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case START_VPN_PROFILE:
                if (resultCode == RESULT_OK)
                    CypherpunkVPN.getInstance().start(getApplicationContext(), getBaseContext(),
                            vpnSetting, accountSetting, vpnServerRepository);
                finish();
                break;
        }
    }
}
