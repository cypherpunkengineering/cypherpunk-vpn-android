package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.ui.main.MainActivity;
import com.cypherpunk.privacy.ui.main.RegionFragment;
import com.cypherpunk.privacy.ui.settings.AccountSettingsFragment;
import com.cypherpunk.privacy.ui.setup.TutorialActivity;
import com.cypherpunk.privacy.ui.signin.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.signin.SignInActivity;
import com.cypherpunk.privacy.ui.signin.SignUpActivity;
import com.cypherpunk.privacy.ui.status.StatusActivity;

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