package com.cypherpunk.android.vpn.dagger;

import com.cypherpunk.android.vpn.ui.MainActivity;
import com.cypherpunk.android.vpn.ui.SignInActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {RealmModule.class, ClientModule.class})
@Singleton
public interface AppComponent {
    void inject(MainActivity target);
    void inject(SignInActivity target);
}