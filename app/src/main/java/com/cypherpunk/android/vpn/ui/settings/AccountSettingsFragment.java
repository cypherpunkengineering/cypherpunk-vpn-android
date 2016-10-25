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

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.model.UserSettingPref;
import com.cypherpunk.android.vpn.ui.account.PremiumFreeActivity;
import com.cypherpunk.android.vpn.ui.setup.IntroductionActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;


public class AccountSettingsFragment extends PreferenceFragmentCompat {

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

        //  set plan
        UserSettingPref user = new UserSettingPref();
        AccountPreference account = (AccountPreference) findPreference("account");
        if (!TextUtils.isEmpty(user.mail)) {
            account.setUsernameText(user.mail);
            account.setRenewal(user.userStatusRenewal);
            account.setExpiration(user.userStatusExpiration);
        }

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

    private void signOut() {
        CypherpunkVPN.getInstance().stop(getActivity().getApplicationContext(), getActivity().getBaseContext());
        UserManager.clearUser();
        Intent intent = new Intent(getActivity(), IntroductionActivity.class);
        TaskStackBuilder builder = TaskStackBuilder.create(getActivity());
        builder.addNextIntent(intent);
        builder.startActivities();
        getActivity().finish();
    }
}
