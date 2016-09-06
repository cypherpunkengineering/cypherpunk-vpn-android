package com.cypherpunk.android.vpn.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.ui.setup.IntroductionActivity;
import com.cypherpunk.android.vpn.vpn.CypherpunkVPN;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getView();
        assert rootView != null;
        RecyclerView list = (RecyclerView) rootView.findViewById(R.id.list);
        list.addItemDecoration(new SettingDividerDecoration(getActivity()));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_settings);

        findPreference("log_out").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                signOut();
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

        findPreference("email").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
        CypherpunkVPN.stop(getActivity().getApplicationContext(), getActivity().getBaseContext());
        UserManager manager = UserManager.getInstance(getActivity());
        manager.clearUser();
        Intent intent = new Intent(getActivity(), IntroductionActivity.class);
        TaskStackBuilder builder = TaskStackBuilder.create(getActivity());
        builder.addNextIntent(intent);
        builder.startActivities();
        getActivity().finish();
    }
}
