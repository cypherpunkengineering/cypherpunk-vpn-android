package com.cypherpunk.privacy.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;

import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.ui.startup.ConfirmationEmailActivity;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.startup.PendingActivity;
import com.cypherpunk.privacy.ui.startup.TutorialActivity;
import com.cypherpunk.privacy.vpn.VpnManager;

import javax.inject.Inject;

public class Navigator {

    private final VpnManager vpnManager;
    private final AccountSetting accountSetting;

    @Inject
    public Navigator(VpnManager vpnManager, AccountSetting accountSetting) {
        this.vpnManager = vpnManager;
        this.accountSetting = accountSetting;
    }

    public void signOut(@NonNull Context context) {
        vpnManager.stop();
        accountSetting.clear();
        TaskStackBuilder.create(context)
                .addNextIntent(IdentifyEmailActivity.createIntent(context))
                .startActivities();
    }

    public void pending(@NonNull Context context) {
        vpnManager.stop();
        TaskStackBuilder.create(context)
                .addNextIntent(PendingActivity.createIntent(context))
                .startActivities();
    }

    public void tutorial(@NonNull Context context) {
        vpnManager.stop();
        TaskStackBuilder.create(context)
                .addNextIntent(TutorialActivity.createIntent(context))
                .startActivities();
    }

    public void confirmEmail(@NonNull Context context, @NonNull String email) {
        vpnManager.stop();
        TaskStackBuilder.create(context)
                .addNextIntent(ConfirmationEmailActivity.createIntent(context, email))
                .startActivities();
    }

    public void tutorialOrPending(@NonNull Context context) {
        if (accountSetting.isPending()) {
            pending(context);
        } else {
            tutorial(context);
        }
    }
}
