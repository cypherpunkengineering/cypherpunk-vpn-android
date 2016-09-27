package com.cypherpunk.android.vpn.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.SettingItem;

import java.util.ArrayList;

public class AdvancedSettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_LIST_SETTING = 1;

    private CypherpunkSetting cypherpunkSetting;
    private Preference protocol;
    private Preference remotePort;
    private Preference localPort;
    private Preference firewall;
    private EncryptionPreference encryptionLevel;
    private Preference cipher;
    private Preference authentication;
    private Preference key;

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

        findPreference("trusted_network").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), NetworkActivity.class));
                return true;
            }
        });

        protocol = findPreference("protocol");
        protocol.setSummary(cypherpunkSetting.protocol);
        protocol.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "protocol", protocol.getTitle(), new CypherpunkSetting().protocol, getSettingItemList(
                                R.array.protocol_value, R.array.protocol_description)),
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
                        "remote_port", remotePort.getTitle(), new CypherpunkSetting().remotePort,
                        getSettingItemList(R.array.remote_port_value, 0)),
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
                        "firewall", firewall.getTitle(), new CypherpunkSetting().firewall,
                        getSettingItemList(R.array.firewall_value, R.array.firewall_description)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        localPort = findPreference("local_port");
        localPort.setSummary(String.valueOf(cypherpunkSetting.localPort));
        localPort.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(LocalPortActivity.createIntent(getActivity(),
                        String.valueOf(cypherpunkSetting.localPort)), REQUEST_LIST_SETTING);
                return true;
            }
        });

        encryptionLevel = (EncryptionPreference) findPreference("encryption_level");
        encryptionLevel.setSummary(cypherpunkSetting.encryptionLevel);
        encryptionLevel.setCipherText(cypherpunkSetting.cipher);
        encryptionLevel.setAuthText(cypherpunkSetting.authentication);
        encryptionLevel.setKeyText(cypherpunkSetting.key);
        encryptionLevel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "encryption_level", encryptionLevel.getTitle(),
                        new CypherpunkSetting().encryptionLevel, getSettingItemList(
                                R.array.encryption_value, R.array.encryption_description)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String updateKey = data.getStringExtra(ListPreferenceActivity.EXTRA_KEY);
            CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
            switch (updateKey) {
                case "protocol":
                    cypherpunkSetting.protocol = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    protocol.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
                    break;
                case "remote_port":
                    cypherpunkSetting.remotePort = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    remotePort.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
                    break;
                case "firewall":
                    cypherpunkSetting.firewall = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    firewall.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
                    break;
                case "encryption_level":
                    cypherpunkSetting.encryptionLevel = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    encryptionLevel.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
                    break;
//                case "cipher":
//                    cypherpunkSetting.cipher = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
//                    cipher.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
//                    break;
//                case "authentication":
//                    cypherpunkSetting.authentication = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
//                    authentication.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
//                    break;
//                case "key":
//                    cypherpunkSetting.key = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
//                    key.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
//                    break;
                case "local_port":
                    cypherpunkSetting.localPort = data.getIntExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE, 8888);
                    localPort.setSummary(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE));
                    break;
            }
            cypherpunkSetting.save();
        }
    }

    private ArrayList<SettingItem> getSettingItemList(@ArrayRes int valueListRes, @ArrayRes int descriptionListRes) {
        String[] valueList = getResources().getStringArray(valueListRes);
        String[] descriptionList = new String[0];
        if (descriptionListRes != 0) {
            descriptionList = getResources().getStringArray(descriptionListRes);
        }
        ArrayList<SettingItem> list = new ArrayList<>();
        for (int i = 0; i < valueList.length; i++) {
            list.add(new SettingItem(valueList[i], descriptionListRes != 0 ? descriptionList[i] : ""));
        }
        return list;
    }
}
