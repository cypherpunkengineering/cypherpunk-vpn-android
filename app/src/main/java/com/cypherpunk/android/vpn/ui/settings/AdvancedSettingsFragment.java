package com.cypherpunk.android.vpn.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.model.SettingItem;

import java.util.ArrayList;

public class AdvancedSettingsFragment extends PreferenceFragmentCompat {

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
        addPreferencesFromResource(R.xml.preference_advanced_settings);

        findPreference("application").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ApplicationSettingsActivity.class));
                return true;
            }
        });

        findPreference("protocol").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(ListPreferenceActivity.createIntent(getActivity(),
                        "Protocol", getSettingItemList(R.array.protocol, R.array.protocol_description)));
                return true;
            }
        });

        findPreference("remote_port").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(ListPreferenceActivity.createIntent(getActivity(),
                        "Remote Port", getSettingItemList(R.array.remote_port, 0)));
                return true;
            }
        });

        findPreference("encryption_level").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(ListPreferenceActivity.createIntent(getActivity(),
                        "Encryption level", getSettingItemList(R.array.encryption, R.array.encryption_description)));
                return true;
            }
        });
    }

    private ArrayList<SettingItem> getSettingItemList(@ArrayRes int titleListRes, @ArrayRes int descriptionListRes) {
        String[] titleList = getResources().getStringArray(titleListRes);
        String[] descriptionList = new String[0];
        if (descriptionListRes != 0) {
            descriptionList = getResources().getStringArray(descriptionListRes);
        }
        ArrayList<SettingItem> list = new ArrayList<>();
        for (int i = 0; i < titleList.length; i++) {
            list.add(new SettingItem(titleList[i], descriptionListRes != 0 ? descriptionList[i] : ""));
        }
        return list;
    }
}
