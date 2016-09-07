package com.cypherpunk.android.vpn.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.SettingItem;

import java.util.ArrayList;

public class AdvancedSettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_LIST_SETTING = 1;

    private CypherpunkSetting cypherpunkSetting;
    private Preference protocol;
    private Preference remotePort;
    private Preference localPort;
    private Preference firewall;
    private Preference encryptionLevel;
    private Preference cipher;
    private Preference authentication;
    private Preference key;

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
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName("cypherpunk_setting");
        addPreferencesFromResource(R.xml.preference_advanced_settings);

        cypherpunkSetting = new CypherpunkSetting();

        findPreference("application").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ApplicationSettingsActivity.class));
                return true;
            }
        });

        protocol = findPreference("protocol");
        protocol.setSummary(cypherpunkSetting.protocol);
        protocol.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "protocol", protocol.getTitle(), cypherpunkSetting.protocol, getSettingItemList(
                                R.array.protocol, R.array.protocol_description, R.array.protocol_value)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        remotePort = findPreference("remote_port");
        remotePort.setSummary(cypherpunkSetting.remotePort);
        remotePort.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "remote_port", remotePort.getTitle(), cypherpunkSetting.remotePort,
                        getSettingItemList(R.array.remote_port, 0, R.array.remote_port_value)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        firewall = findPreference("firewall");
        firewall.setSummary(cypherpunkSetting.firewall);
        firewall.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "firewall", firewall.getTitle(), cypherpunkSetting.firewall,
                        getSettingItemList(R.array.firewall, R.array.firewall_description, R.array.firewall_value)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        localPort = findPreference("local_port");
        localPort.setSummary(cypherpunkSetting.local_port);

        encryptionLevel = findPreference("encryption_level");
        encryptionLevel.setSummary(cypherpunkSetting.encryptionLevel);
        encryptionLevel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "encryption_level", encryptionLevel.getTitle(), cypherpunkSetting.encryptionLevel, getSettingItemList(
                                R.array.encryption, R.array.encryption_description, R.array.encryption_value)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        cipher = findPreference("cipher");
        cipher.setSummary(cypherpunkSetting.cipher);

        authentication = findPreference("authentication");
        authentication.setSummary(cypherpunkSetting.authentication);

        key = findPreference("key");
        key.setSummary(cypherpunkSetting.key);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String key = data.getStringExtra(ListPreferenceActivity.EXTRA_KEY);
            CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
            switch (key) {
                case "protocol":
                    cypherpunkSetting.protocol = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    protocol.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_NAME));
                    break;
                case "remote_port":
                    cypherpunkSetting.remotePort = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    remotePort.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_NAME));
                    break;
                case "firewall":
                    cypherpunkSetting.firewall = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    firewall.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_NAME));
                    break;
                case "encryption_level":
                    cypherpunkSetting.encryptionLevel = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    encryptionLevel.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_NAME));
                    break;
            }
            cypherpunkSetting.save();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private ArrayList<SettingItem> getSettingItemList(@ArrayRes int titleListRes, @ArrayRes int descriptionListRes, @ArrayRes int valueListRes) {
        String[] titleList = getResources().getStringArray(titleListRes);
        String[] valueList = getResources().getStringArray(valueListRes);
        String[] descriptionList = new String[0];
        if (descriptionListRes != 0) {
            descriptionList = getResources().getStringArray(descriptionListRes);
        }
        ArrayList<SettingItem> list = new ArrayList<>();
        for (int i = 0; i < titleList.length; i++) {
            list.add(new SettingItem(titleList[i], descriptionListRes != 0 ? descriptionList[i] : "", valueList[i]));
        }
        return list;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        cypherpunkSetting = new CypherpunkSetting();
        if (key.equals("local_port")) {
            localPort.setSummary(cypherpunkSetting.local_port);
        }
    }
}
