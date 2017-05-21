package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.ui.account.AccountSettingsFragment;
import com.cypherpunk.privacy.ui.account.EditEmailActivity;
import com.cypherpunk.privacy.ui.account.EditPasswordActivity;
import com.cypherpunk.privacy.ui.account.UpgradePlanActivity;
import com.cypherpunk.privacy.ui.main.CypherpunkLaunchVPN;
import com.cypherpunk.privacy.ui.main.CypherpunkWifiReceiver;
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
import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {VpnServerModule.class, ClientModule.class, SettingModule.class})
@Singleton
public interface AppComponent {

    void inject(LoginActivity target);

    void inject(SignUpActivity target);

    void inject(AccountSettingsFragment target);

    void inject(IdentifyEmailActivity target);

    void inject(ConfirmationEmailActivity target);

    void inject(RegionFragment target);

    void inject(TutorialActivity target);

    void inject(EditEmailActivity target);

    void inject(EditPasswordActivity target);

    void inject(UpgradePlanActivity target);

    void inject(SplitTunnelActivity target);

    void inject(CypherpunkLaunchVPN target);

    void inject(TunnelModeActivity target);

    void inject(CypherpunkWifiReceiver target);

    void inject(MainActivity target);

    void inject(InternetKillSwitchActivity target);

    void inject(NetworkActivity target);

    void inject(RemotePortActivity target);

    void inject(SettingsFragment target);

    void inject(CypherpunkVpnStatus target);
}
