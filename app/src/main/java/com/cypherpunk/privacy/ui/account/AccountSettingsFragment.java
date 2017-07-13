package com.cypherpunk.privacy.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.account.Subscription;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.ui.common.Urls;
import com.cypherpunk.privacy.ui.startup.IdentifyEmailActivity;
import com.cypherpunk.privacy.vpn.VpnManager;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_UPGRADE_PLAN = 1;
    private static final int REQUEST_CODE_EDIT_EMAIL = 2;

    private AccountPreference accountPreference;

    @NonNull
    private Disposable disposable = Disposables.empty();

    @Inject
    NetworkRepository networkRepository;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnManager vpnManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView headerView = ButterKnife.findById(view, R.id.header);
        headerView.setText(R.string.account_setting_header_account);

        final FrameLayout frameLayout = ButterKnife.findById(view, R.id.list_container);
        final View list = super.onCreateView(inflater, frameLayout, savedInstanceState);
        frameLayout.addView(list);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setDivider(ContextCompat.getDrawable(getContext(), R.drawable.divider));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        CypherpunkApplication.instance.getAppComponent().inject(this);

        addPreferencesFromResource(R.xml.preference_account);

        //  set plan
        final Subscription subscription = accountSetting.subscription();

        accountPreference = (AccountPreference) findPreference(getString(R.string.account_preference_account));
        accountPreference.setInfo(accountSetting.email(), accountSetting.accountType(), subscription);

        getStatus();

        final Preference upgrade = findPreference(getString(R.string.account_preference_upgrade));
        upgrade.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://cypherpunk.com/account?secret=foo")));
//                startActivityForResult(new Intent(getContext(), UpgradePlanActivity.class), REQUEST_CODE_UPGRADE_PLAN);
                return true;
            }
        });

        // email
        final Preference email = findPreference(getString(R.string.account_preference_edit_email));
        email.setVisible(false);
        email.setSummary(accountSetting.email());
        email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(EditEmailActivity.createIntent(getContext()), REQUEST_CODE_EDIT_EMAIL);
                return true;
            }
        });

        // password
        final Preference password = findPreference(getString(R.string.account_preference_edit_password));
        password.setVisible(false);
        password.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
                        vpnManager.stop();
                        accountSetting.clear();
                        TaskStackBuilder.create(getContext())
                                .addNextIntent(IdentifyEmailActivity.createIntent(getContext()))
                                .startActivities();
                        getActivity().finish();
                        return true;
                    }
                });

        // Terms of Service
        findPreference(getString(R.string.account_preference_terms_of_service))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.TERMS_OF_SERVICE)));
                        return true;
                    }
                });

        // Privacy Policy
        findPreference(getString(R.string.account_preference_privacy_policy))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.PRIVACY_POLICY)));
                        return true;
                    }
                });

        // License Information
        findPreference(getString(R.string.account_preference_license_information))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Toast.makeText(getContext(), "not yet implemented", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_UPGRADE_PLAN: {
                    final Subscription subscription = accountSetting.subscription();

                    accountPreference.setInfo(accountSetting.email(), accountSetting.accountType(),
                            subscription);

                    final Subscription.Type type = subscription.type();

                    final boolean upgradeVisible = type != Subscription.Type.ANNUALLY
                            && type != Subscription.Type.FOREVER;
                    findPreference("upgrade").setVisible(upgradeVisible);
                    break;
                }
                case REQUEST_CODE_EDIT_EMAIL: {
                    accountPreference.setUsernameText(accountSetting.email());
                    break;
                }
            }
        }
    }

    private void getStatus() {
        disposable = networkRepository.getAccountStatus()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<StatusResult>() {
                    @Override
                    public void onSuccess(StatusResult accountStatus) {
                        accountSetting.updateAccount(accountStatus.account);
                        accountSetting.updateSubscription(accountStatus.subscription);

                        accountPreference.setInfo(accountSetting.email(), accountStatus.account.type(),
                                accountStatus.subscription);
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
