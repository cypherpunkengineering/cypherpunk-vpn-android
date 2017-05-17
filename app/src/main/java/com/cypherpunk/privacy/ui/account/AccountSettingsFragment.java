package com.cypherpunk.privacy.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.AccountStatusResult;
import com.cypherpunk.privacy.model.UserSettingPref;
import com.cypherpunk.privacy.ui.common.Urls;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.vpn.CypherpunkVPN;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_UPGRADE_PLAN = 1;
    private static final int REQUEST_CODE_EDIT_EMAIL = 2;

    private AccountPreference accountPreference;

    @NonNull
    private Subscription subscription = Subscriptions.empty();

    @Inject
    CypherpunkService webService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setDivider(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.divider)));
        setDividerHeight(getContext().getResources().getDimensionPixelSize(R.dimen.divider));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_account);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        //  set plan
        final UserSettingPref userSettingPref = new UserSettingPref();

        accountPreference = (AccountPreference) findPreference(getString(R.string.account_preference_account));
        if (!TextUtils.isEmpty(userSettingPref.userStatusRenewal)) {
            accountPreference.setUsernameText(userSettingPref.mail);
            accountPreference.setType(userSettingPref.userStatusType);
            accountPreference.setRenewalAndExpiration(userSettingPref.userStatusRenewal, userSettingPref.userStatusExpiration);
        }

        getStatus();

        boolean upgradeVisible = !"annually".equals(userSettingPref.userStatusRenewal) &&
                !"forever".equals(userSettingPref.userStatusRenewal) &&
                !"lifetime".equals(userSettingPref.userStatusRenewal);
        final Preference upgrade = findPreference(getString(R.string.account_preference_upgrade));
        upgrade.setVisible(upgradeVisible);
        upgrade.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(getContext(), UpgradePlanActivity.class), REQUEST_CODE_UPGRADE_PLAN);
                return true;
            }
        });

        // email
        final Preference email = findPreference(getString(R.string.account_preference_edit_email));
        email.setSummary(UserManager.getMailAddress());
        email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(EditEmailActivity.createIntent(getContext()), REQUEST_CODE_EDIT_EMAIL);
                return true;
            }
        });

        findPreference(getString(R.string.account_preference_edit_password))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(EditPasswordActivity.createIntent(getContext()));
                        return true;
                    }
                });

        findPreference(getString(R.string.account_preference_share))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(ShareActivity.createIntent(getContext()));
                        return true;
                    }
                });

        findPreference(getString(R.string.account_preference_rate))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.STORE)));
                        return true;
                    }
                });

        // Open a Support Ticket
        findPreference(getString(R.string.account_preference_support))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.CONTACT_US)));
                        return true;
                    }
                });

        // Go to Help Center
        findPreference(getString(R.string.account_preference_help))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.HELP)));
                        return true;
                    }
                });

        findPreference(getString(R.string.account_preference_sign_out))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        CypherpunkVPN.getInstance().stop();
                        UserManager.clearUser();
                        TaskStackBuilder.create(getContext())
                                .addNextIntent(IdentifyEmailActivity.createIntent(getContext()))
                                .startActivities();
                        getActivity().finish();
                        return true;
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            final UserSettingPref userSettingPref = new UserSettingPref();
            switch (requestCode) {
                case REQUEST_CODE_UPGRADE_PLAN: {
                    final String renewal = userSettingPref.userStatusRenewal;

                    accountPreference.setUsernameText(userSettingPref.mail);
                    accountPreference.setType(userSettingPref.userStatusType);
                    accountPreference.setRenewalAndExpiration(renewal, userSettingPref.userStatusExpiration);

                    boolean upgradeVisible = !"annually".equals(renewal) &&
                            !"forever".equals(renewal) &&
                            !"lifetime".equals(renewal);
                    findPreference("upgrade").setVisible(upgradeVisible);
                    break;
                }
                case REQUEST_CODE_EDIT_EMAIL: {
                    accountPreference.setUsernameText(userSettingPref.mail);
                    break;
                }
            }
        }
    }

    private void getStatus() {
        subscription = webService.getAccountStatus()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<AccountStatusResult>() {
                    @Override
                    public void onSuccess(AccountStatusResult accountStatus) {
                        final String type = accountStatus.getAccount().type;
                        final String renewal = accountStatus.getSubscription().renewal;
                        final String expiration = accountStatus.getSubscription().expiration;

                        final UserSettingPref userSettingPref = new UserSettingPref();
                        userSettingPref.userStatusType = type;
                        userSettingPref.userStatusRenewal = renewal;
                        userSettingPref.userStatusExpiration = expiration;
                        userSettingPref.save();

                        accountPreference.setUsernameText(userSettingPref.mail);
                        accountPreference.setType(type);
                        accountPreference.setRenewalAndExpiration(renewal, expiration);
                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                        if (error instanceof HttpException) {
                            HttpException httpException = (HttpException) error;
                            if (httpException.code() == 400) {
                                TaskStackBuilder.create(getContext())
                                        .addNextIntent(IdentifyEmailActivity.createIntent(getContext()))
                                        .startActivities();
                            }
                        }
                    }
                });
    }
}
