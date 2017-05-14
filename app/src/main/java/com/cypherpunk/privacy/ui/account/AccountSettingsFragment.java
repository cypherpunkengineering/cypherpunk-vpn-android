package com.cypherpunk.privacy.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.AccountStatusResult;
import com.cypherpunk.privacy.model.UserSettingPref;
import com.cypherpunk.privacy.ui.common.Urls;
import com.cypherpunk.privacy.ui.signin.IdentifyEmailActivity;
import com.cypherpunk.privacy.vpn.CypherpunkVPN;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_UPGRADE_PLAN = 1;
    private static final int REQUEST_EDIT_EMAIL = 2;

    private AccountPreference accountPreference;
    private Subscription subscription = Subscriptions.empty();
    private boolean confirmed;

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
            accountPreference.setType(user.userStatusType);
            accountPreference.setRenewalAndExpiration(user.userStatusRenewal, user.userStatusExpiration);
        }

        getStatus();

        // email
        Preference email = findPreference("email");
        email.setSummary(UserManager.getMailAddress());

        findPreference("share").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(ShareActivity.createIntent(getContext()));
                return true;
            }
        });

        findPreference("upgrade").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(getActivity(), UpgradePlanActivity.class), REQUEST_UPGRADE_PLAN);
                return true;
            }
        });
        boolean upgradeVisible = !"annually".equals(user.userStatusRenewal) &&
                !"forever".equals(user.userStatusRenewal) &&
                !"lifetime".equals(user.userStatusRenewal);
        findPreference("upgrade").setVisible(upgradeVisible);

        email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(getActivity(), EditEmailActivity.class), REQUEST_EDIT_EMAIL);
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

        findPreference("rate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.STORE)));
                return true;
            }
        });

        // Open a Support Ticket
        findPreference("contact_us").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.CONTACT_US)));
                return true;
            }
        });

        // Go to Help Center
        findPreference("help").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.HELP)));
                return true;
            }
        });

        findPreference("log_out").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                signOut();
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
            switch (requestCode) {
                case REQUEST_UPGRADE_PLAN: {
                    UserSettingPref statusPref = new UserSettingPref();

                    final String renewal = statusPref.userStatusRenewal;

                    accountPreference.setUsernameText(statusPref.mail);
                    accountPreference.setType(statusPref.userStatusType);
                    accountPreference.setRenewalAndExpiration(renewal, statusPref.userStatusExpiration);

                    boolean upgradeVisible = !"annually".equals(renewal) &&
                            !"forever".equals(renewal) &&
                            !"lifetime".equals(renewal);
                    findPreference("upgrade").setVisible(upgradeVisible);
                    break;
                }
                case REQUEST_EDIT_EMAIL: {
                    UserSettingPref statusPref = new UserSettingPref();
                    accountPreference.setUsernameText(statusPref.mail);
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

                        UserSettingPref statusPref = new UserSettingPref();
                        statusPref.userStatusType = type;
                        statusPref.userStatusRenewal = renewal;
                        statusPref.userStatusExpiration = expiration;
                        statusPref.save();

                        /*if (!confirmed) {
                            startActivity(ConfirmationEmailActivity.createIntent(getActivity(), statusPref.mail));
                        }*/

                        accountPreference.setUsernameText(statusPref.mail);
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

    private void signOut() {
        CypherpunkVPN.getInstance().stop(getActivity().getApplicationContext(), getActivity().getBaseContext());
        UserManager.clearUser();
        TaskStackBuilder.create(getContext())
                .addNextIntent(IdentifyEmailActivity.createIntent(getContext()))
                .startActivities();
        getActivity().finish();
    }
}
