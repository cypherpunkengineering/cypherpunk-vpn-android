package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.ui.account.AccountCheckJobService;
import com.cypherpunk.privacy.ui.account.AccountSettingsFragment;
import com.cypherpunk.privacy.ui.account.BillingActivity;
import com.cypherpunk.privacy.ui.account.EditEmailActivity;
import com.cypherpunk.privacy.ui.account.EditPasswordActivity;
import com.cypherpunk.privacy.ui.account.UpgradePlanActivity;
import com.cypherpunk.privacy.ui.main.MainActivity;
import com.cypherpunk.privacy.ui.region.RegionFragment;
import com.cypherpunk.privacy.ui.settings.InternetKillSwitchActivity;
import com.cypherpunk.privacy.ui.settings.NetworkActivity;
import com.cypherpunk.privacy.ui.settings.RemotePortActivity;
import com.cypherpunk.privacy.ui.settings.SettingsFragment;
import com.cypherpunk.privacy.ui.settings.SplitTunnelActivity;
import com.cypherpunk.privacy.ui.settings.TunnelModeActivity;
import com.cypherpunk.privacy.ui.startup.ConfirmationEmailActivity;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.startup.LoginActivity;
import com.cypherpunk.privacy.ui.startup.SignUpActivity;
import com.cypherpunk.privacy.ui.startup.TutorialActivity;
import com.cypherpunk.privacy.ui.vpn.ConnectionFragment;
import com.cypherpunk.privacy.ui.vpn.VpnConnectionIntentService;
import com.cypherpunk.privacy.ui.vpn.VpnStartActivity;
import com.cypherpunk.privacy.ui.vpn.CypherpunkTileService;
import com.cypherpunk.privacy.ui.vpn.CypherpunkWifiReceiver;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {VpnModule.class, VpnServerModule.class, ClientModule.class, SettingModule.class})
@Singleton
public interface AppComponent {

    // Activity

    void inject(IdentifyEmailActivity target);

    void inject(ConfirmationEmailActivity target);

    void inject(LoginActivity target);

    void inject(SignUpActivity target);

    void inject(TutorialActivity target);

    void inject(EditEmailActivity target);

    void inject(EditPasswordActivity target);

    void inject(UpgradePlanActivity target);

    void inject(SplitTunnelActivity target);

    void inject(TunnelModeActivity target);

    void inject(InternetKillSwitchActivity target);

    void inject(NetworkActivity target);

    void inject(RemotePortActivity target);

    void inject(MainActivity target);

    void inject(VpnStartActivity target);

    // Fragment

    void inject(SettingsFragment target);

    void inject(AccountSettingsFragment target);

    void inject(RegionFragment target);

    void inject(ConnectionFragment target);

    // Service

    void inject(CypherpunkTileService target);

    void inject(VpnConnectionIntentService target);

    // BroadcastReceiver

    void inject(CypherpunkWifiReceiver target);

    void inject(BillingActivity target);

    void inject(AccountCheckJobService target);

}
