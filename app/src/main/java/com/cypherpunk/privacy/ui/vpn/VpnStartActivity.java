package com.cypherpunk.privacy.ui.vpn;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.vpn.VpnManager;
import com.cypherpunk.privacy.vpn.VpnStatusHolder;

import javax.inject.Inject;

public class VpnStartActivity extends Activity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        final Intent intent = new Intent(context, VpnStartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    private static final int REQUEST_CODE_VPN_PROFILE = 70;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    VpnStatusHolder vpnStatusHolder;

    @Inject
    VpnManager vpnManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        final Intent intent = VpnService.prepare(this);
        if (intent == null) {
            finish();
            return;
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_VPN_PROFILE);
        } catch (ActivityNotFoundException e) {
            // buggy sony devices
            e.printStackTrace();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VPN_PROFILE && resultCode == RESULT_OK) {
            final VpnServer vpnServer = vpnServerRepository.find(vpnSetting.regionId());
            if (vpnServer != null) {
                vpnManager.start(this, vpnServer);
            }
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // crash fix for invisible dialog, in case perm dialog needs to be shown
        setVisible(true);
    }
}
