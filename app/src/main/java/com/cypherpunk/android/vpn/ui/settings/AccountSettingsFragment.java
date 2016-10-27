package com.cypherpunk.android.vpn.ui.settings;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;

import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.CypherpunkService;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.data.api.json.LoginResult;
import com.cypherpunk.android.vpn.data.api.json.StatusResult;
import com.cypherpunk.android.vpn.model.UserSettingPref;
import com.cypherpunk.android.vpn.ui.account.PremiumFreeActivity;
import com.cypherpunk.android.vpn.ui.signin.IdentifyEmailActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;

import javax.inject.Inject;

import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private AccountPreference accountPreference;
    private Subscription subscription = Subscriptions.empty();

    @Inject
    CypherpunkService webService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getView();
        assert rootView != null;
        setDivider(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.divider)));
        setDividerHeight(getContext().getResources().getDimensionPixelSize(R.dimen.divider));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_account);
        CypherpunkApplication.instance.getAppComponent().inject(this);

        //  set plan
        UserSettingPref user = new UserSettingPref();
        accountPreference = (AccountPreference) findPreference("account");
        if (!TextUtils.isEmpty(user.userStatusRenewal)) {
            accountPreference.setUsernameText(user.mail);
            accountPreference.setRenewal(user.userStatusRenewal);
            accountPreference.setExpiration(user.userStatusExpiration);
        }

        getStatus();

        // email
        Preference email = findPreference("email");
        email.setSummary(UserManager.getMailAddress());

        findPreference("log_out").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                signOut();
                return true;
            }
        });

        // TODO: すでに課金していたら表示しなくて良さそう
        findPreference("premium_free").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), PremiumFreeActivity.class));
                return true;
            }
        });

        findPreference("upgrade").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), UpgradePlanActivity.class));
                return true;
            }
        });

        findPreference("contact_us").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ContactActivity.class));
                return true;
            }
        });

        email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), EditEmailActivity.class));
                return true;
            }
        });

        findPreference("password").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), EditPasswordActivity.class));
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }

    private void getStatus() {
        subscription = webService
                .login(new LoginRequest(UserManager.getMailAddress(), UserManager.getPassword()))
                .flatMap(new Func1<LoginResult, Single<StatusResult>>() {
                    @Override
                    public Single<StatusResult> call(LoginResult result) {
                        return webService.getStatus();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<StatusResult>() {
                    @Override
                    public void onSuccess(StatusResult status) {
                        UserSettingPref statusPref = new UserSettingPref();
                        statusPref.userStatusType = status.getType();
                        statusPref.userStatusRenewal = status.getRenewal();
                        statusPref.userStatusExpiration = status.getExpiration();
                        statusPref.save();

                        accountPreference.setUsernameText(statusPref.mail);
                        accountPreference.setRenewal(status.getRenewal());
                        accountPreference.setExpiration(status.getExpiration());
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    private void signOut() {
        CypherpunkVPN.getInstance().stop(getActivity().getApplicationContext(), getActivity().getBaseContext());
        UserManager.clearUser();
        Intent intent = new Intent(getActivity(), IdentifyEmailActivity.class);
        TaskStackBuilder builder = TaskStackBuilder.create(getActivity());
        builder.addNextIntent(intent);
        builder.startActivities();
        getActivity().finish();
    }
}
