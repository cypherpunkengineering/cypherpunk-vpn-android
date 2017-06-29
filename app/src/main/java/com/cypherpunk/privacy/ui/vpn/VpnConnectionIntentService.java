package com.cypherpunk.privacy.ui.vpn;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.vpn.VpnManager;
import com.cypherpunk.privacy.vpn.VpnStatusHolder;

import javax.inject.Inject;

import timber.log.Timber;

public class VpnConnectionIntentService extends IntentService {

    public enum Mode {
        AUTO_START,
        TILE,
        TRUSTED,
        UNTRUSTED
    }

    private static final String EXTRA_MODE = "mode";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull Mode mode) {
        final Intent intent = new Intent(context, VpnConnectionIntentService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    VpnStatusHolder vpnStatusHolder;

    @Inject
    VpnManager vpnManager;

    public VpnConnectionIntentService() {
        super("VpnConnectionIntentService");
        CypherpunkApplication.instance.getAppComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.d("handleIntent()");

        // check if user is signed in
        if (!accountSetting.isSignedIn() || intent == null) {
            Timber.d("user not logged in, ignoring intent");
            return;
        }

        final Mode mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
        if (mode == null) {
            return;
        }

        switch (mode) {
            case AUTO_START:
                if (vpnSetting.isAutoConnect()) {
                    Timber.d("auto starting VPN");
                    prepareVpnService();
                }
                break;

            case TILE:
                // either connecting or already connected, stop vpn and finish activity
                if (!vpnStatusHolder.isDisconnected()) {
                    vpnManager.stop();
                } else {
                    prepareVpnService();
                }
                break;

            case TRUSTED:
                vpnManager.stop();
                break;

            case UNTRUSTED:
                prepareVpnService();
                break;
        }
    }

    private void prepareVpnService() {
        Timber.d("prepareVpnService()");

        final Intent intent = VpnService.prepare(this);
        if (intent == null) {
            final VpnServer vpnServer = vpnServerRepository.find(vpnSetting.regionId());
            if (vpnServer != null) {
                vpnManager.start(this, vpnServer);
            }
        } else {
            startActivity(VpnStartActivity.createIntent(this));
        }
    }
}
