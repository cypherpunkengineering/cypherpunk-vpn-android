package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.ui.account.AccountSettingsFragment;
import com.cypherpunk.privacy.ui.account.EditEmailActivity;
import com.cypherpunk.privacy.ui.account.EditPasswordActivity;
import com.cypherpunk.privacy.ui.account.UpgradePlanActivity;
import com.cypherpunk.privacy.ui.region.RegionFragment;
import com.cypherpunk.privacy.ui.setup.TutorialActivity;
import com.cypherpunk.privacy.ui.signin.ConfirmationEmailActivity;
import com.cypherpunk.privacy.ui.signin.IdentifyEmailActivity;
import com.cypherpunk.privacy.ui.signin.LoginActivity;
import com.cypherpunk.privacy.ui.signin.SignUpActivity;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.Realm;

@Component(modules = {RealmModule.class, ClientModule.class})
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

    Realm getDefaultRealm();
}
