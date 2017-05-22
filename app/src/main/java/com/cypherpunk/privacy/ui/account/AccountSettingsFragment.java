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

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.AccountStatusResult;
import com.cypherpunk.privacy.domain.model.Plan;
import com.cypherpunk.privacy.model.UserSetting;
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
        final UserSetting userSetting = UserSetting.instance();

        final Plan plan = userSetting.subscriptionPlan();

        accountPreference = (AccountPreference) findPreference(getString(R.string.account_preference_account));
        accountPreference.setInfo(userSetting.mail(), userSetting.accountType(), plan);

        getStatus();

        final Plan.Renewal renewal = plan.renewal();

        final boolean upgradeVisible = renewal != Plan.Renewal.ANNUALLY
                && renewal != Plan.Renewal.FOREVER
                && renewal != Plan.Renewal.LIFETIME;
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
        email.setSummary(userSetting.mail());
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
                        UserSetting.instance().clear();
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
            final UserSetting userSetting = UserSetting.instance();
            switch (requestCode) {
                case REQUEST_CODE_UPGRADE_PLAN: {
                    final Plan plan = userSetting.subscriptionPlan();

                    accountPreference.setInfo(userSetting.mail(), userSetting.accountType(),
                            userSetting.subscriptionPlan());

                    final Plan.Renewal renewal = plan.renewal();

                    final boolean upgradeVisible = renewal != Plan.Renewal.ANNUALLY
                            && renewal != Plan.Renewal.FOREVER
                            && renewal != Plan.Renewal.LIFETIME;
                    findPreference("upgrade").setVisible(upgradeVisible);
                    break;
                }
                case REQUEST_CODE_EDIT_EMAIL: {
                    accountPreference.setUsernameText(userSetting.mail());
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

                        final UserSetting userSetting = UserSetting.instance();
                        userSetting.updateStatus(type, renewal, expiration);

                        accountPreference.setInfo(userSetting.mail(), userSetting.accountType(),
                                userSetting.subscriptionPlan());
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
