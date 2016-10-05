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
import com.cypherpunk.android.vpn.vpn.CypherpunkVpnStatus;

import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_LIST_SETTING = 1;

    private CypherpunkSetting cypherpunkSetting;
    private Preference protocol;
    private Preference remotePort;
    private Preference vpnPortLocal;
    private Preference firewall;
    private EncryptionPreference vpnCryptoProfile;

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
        addPreferencesFromResource(R.xml.preference_settings);

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

        protocol = findPreference("vpn_protocol");
        protocol.setSummary(getStringByKey(cypherpunkSetting.vpnProtocol));
        protocol.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "vpn_protocol", protocol.getTitle(), new CypherpunkSetting().vpnProtocol, getSettingItemList(
                                R.array.vpn_protocol_value, R.array.vpn_protocol_description)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        remotePort = findPreference("vpn_port_remote");
        remotePort.setSummary(getStringByKey(cypherpunkSetting.vpnPortRemote));
        remotePort.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "vpn_port_remote", remotePort.getTitle(), new CypherpunkSetting().vpnPortRemote,
                        getSettingItemList(R.array.vpn_port_remote_value, 0)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        firewall = findPreference("privacy_firewall_mode");
        firewall.setSummary(getStringByKey(cypherpunkSetting.privacyFirewallMode));
        firewall.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "privacy_firewall_mode", firewall.getTitle(), new CypherpunkSetting().privacyFirewallMode,
                        getSettingItemList(R.array.privacy_firewall_mode_value, R.array.privacy_firewall_mode_description)),
                        REQUEST_LIST_SETTING);
                return true;
            }
        });

        vpnPortLocal = findPreference("vpn_port_local");
        vpnPortLocal.setSummary(String.valueOf(cypherpunkSetting.vpnPortLocal));
        vpnPortLocal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(LocalPortActivity.createIntent(getActivity(),
                        String.valueOf(cypherpunkSetting.vpnPortLocal)), REQUEST_LIST_SETTING);
                return true;
            }
        });

        vpnCryptoProfile = (EncryptionPreference) findPreference("vpn_crypto_profile");
        vpnCryptoProfile.setSummary(getStringByKey(cypherpunkSetting.vpnCryptoProfile));
        vpnCryptoProfile.setCipherText(getStringByKey(cypherpunkSetting.vpnCryptoProfileCipher));
        vpnCryptoProfile.setAuthText(getStringByKey(cypherpunkSetting.vpnCryptoProfileAuth));
        vpnCryptoProfile.setKeyText(getStringByKey(cypherpunkSetting.vpnCryptoProfileKeylen));
        vpnCryptoProfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(ListPreferenceActivity.createIntent(getActivity(),
                        "vpn_crypto_profile", vpnCryptoProfile.getTitle(),
                        new CypherpunkSetting().vpnCryptoProfile, getSettingItemList(
                                R.array.vpn_crypto_profile_value, R.array.vpn_crypto_profile_description)),
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
                case "vpn_protocol":
                    cypherpunkSetting.vpnProtocol = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    protocol.setSummary(getStringByKey(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE)));
                    break;
                case "vpn_port_remote":
                    cypherpunkSetting.vpnPortRemote = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    remotePort.setSummary(getStringByKey(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE)));
                    break;
                case "privacy_firewall_mode":
                    cypherpunkSetting.privacyFirewallMode = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    firewall.setSummary(getStringByKey(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE)));
                    break;
                case "vpn_crypto_profile":
                    cypherpunkSetting.vpnCryptoProfile = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    String stringExtra = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    vpnCryptoProfile.setSummary(getStringByKey(stringExtra));
                    String cipher;
                    String auth;
                    String keylen;
                    // TODO:
                    switch (stringExtra) {
                        case "setting_vpn_crypto_profile_default":
                            cipher = getStringByKey("setting_vpn_crypto_cipher_aes128cbc");
                            auth = getStringByKey("setting_vpn_crypto_auth_sha256");
                            keylen = getStringByKey("setting_vpn_crypto_keylen_rsa4096");
                            cypherpunkSetting.vpnCryptoProfileCipher = cipher;
                            cypherpunkSetting.vpnCryptoProfileAuth = auth;
                            cypherpunkSetting.vpnCryptoProfileKeylen = keylen;
                            vpnCryptoProfile.setCipherText(cipher);
                            vpnCryptoProfile.setAuthText(auth);
                            vpnCryptoProfile.setKeyText(keylen);
                            break;
                        case "setting_vpn_crypto_profile_none":
                            cipher = getStringByKey("setting_vpn_crypto_cipher_none");
                            auth = getStringByKey("setting_vpn_crypto_auth_sha1");
                            keylen = getStringByKey("setting_vpn_crypto_keylen_rsa4096");
                            cypherpunkSetting.vpnCryptoProfileCipher = cipher;
                            cypherpunkSetting.vpnCryptoProfileAuth = auth;
                            cypherpunkSetting.vpnCryptoProfileKeylen = keylen;
                            vpnCryptoProfile.setCipherText(cipher);
                            vpnCryptoProfile.setAuthText(auth);
                            vpnCryptoProfile.setKeyText(keylen);
                            break;
                        case "setting_vpn_crypto_profile_strong":
                            cipher = getStringByKey("setting_vpn_crypto_cipher_aes256cbc");
                            auth = getStringByKey("setting_vpn_crypto_auth_sha512");
                            keylen = getStringByKey("setting_vpn_crypto_keylen_rsa4096");
                            cypherpunkSetting.vpnCryptoProfileCipher = cipher;
                            cypherpunkSetting.vpnCryptoProfileAuth = auth;
                            cypherpunkSetting.vpnCryptoProfileKeylen = keylen;
                            vpnCryptoProfile.setCipherText(cipher);
                            vpnCryptoProfile.setAuthText(auth);
                            vpnCryptoProfile.setKeyText(keylen);
                            break;
                        case "setting_vpn_crypto_profile_stealth":
                            cipher = getStringByKey("setting_vpn_crypto_cipher_aes128cbc");
                            auth = getStringByKey("setting_vpn_crypto_auth_sha256");
                            keylen = getStringByKey("setting_vpn_crypto_keylen_rsa4096");
                            cypherpunkSetting.vpnCryptoProfileCipher = cipher;
                            cypherpunkSetting.vpnCryptoProfileAuth = auth;
                            cypherpunkSetting.vpnCryptoProfileKeylen = keylen;
                            vpnCryptoProfile.setCipherText(cipher);
                            vpnCryptoProfile.setAuthText(auth);
                            vpnCryptoProfile.setKeyText(keylen);
                            break;
                    }
                    break;
                case "vpn_port_local":
                    cypherpunkSetting.vpnPortLocal = data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE);
                    vpnPortLocal.setSummary(getStringByKey(data.getStringExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE)));
                    break;
            }
            cypherpunkSetting.save();

            CypherpunkVpnStatus vpnStatus = new CypherpunkVpnStatus();
            if (vpnStatus.isConnected()) {
                SettingConnectDialogFragment dialogFragment = SettingConnectDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager());
            }
        }
    }

    private String getStringByKey(String key)
    {
        String packageName = getContext().getPackageName();
        int id = getResources().getIdentifier(key, "string", packageName);
        String str;
        try
        {
            str = getResources().getString(id);
        }
        catch (Exception NotFoundException)
        {
            str = key;
        }
        return str;
    }

    private ArrayList<SettingItem> getSettingItemList(@ArrayRes int keyListRes, @ArrayRes int descriptionListRes) {
        String[] keyList = getResources().getStringArray(keyListRes);
        String[] descriptionList = new String[0];
        if (descriptionListRes != 0) {
            descriptionList = getResources().getStringArray(descriptionListRes);
        }
        ArrayList<SettingItem> list = new ArrayList<>();
        for (int i = 0; i < keyList.length; i++) {
            list.add(new SettingItem(keyList[i], getStringByKey(keyList[i]), descriptionListRes != 0 ? getStringByKey(descriptionList[i]) : ""));
        }
        return list;
    }
}
