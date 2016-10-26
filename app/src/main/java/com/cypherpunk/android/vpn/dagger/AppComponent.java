package com.cypherpunk.android.vpn.dagger;

import com.cypherpunk.android.vpn.ui.main.MainActivity;
import com.cypherpunk.android.vpn.ui.main.RegionFragment;
import com.cypherpunk.android.vpn.ui.settings.AccountSettingsFragment;
import com.cypherpunk.android.vpn.ui.setup.TutorialActivity;
import com.cypherpunk.android.vpn.ui.signin.IdentifyEmailActivity;
import com.cypherpunk.android.vpn.ui.signin.SignInActivity;
import com.cypherpunk.android.vpn.ui.signin.SignUpActivity;
import com.cypherpunk.android.vpn.ui.status.StatusActivity;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.Realm;

@Component(modules = {RealmModule.class, ClientModule.class})
@Singleton
public interface AppComponent {

    void inject(MainActivity target);

    void inject(SignInActivity target);

    void inject(SignUpActivity target);

    void inject(StatusActivity target);

    void inject(AccountSettingsFragment target);

    void inject(IdentifyEmailActivity target);

    void inject(RegionFragment target);

    void inject(TutorialActivity target);

    Realm getDefaultRealm();
}
