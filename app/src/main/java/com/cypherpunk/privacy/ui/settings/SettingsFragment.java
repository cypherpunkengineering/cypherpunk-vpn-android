package com.cypherpunk.privacy.ui.settings;

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

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.SettingItem;
import com.cypherpunk.privacy.utils.ResourceUtil;
import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;

import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_LIST_SETTING = 1;
    private static final int REQUEST_CODE_INTERNET_KILL_SWITCH = 2;
    private static final int REQUEST_CODE_TUNNEL_MODE = 3;
    private static final int REQUEST_CODE_REMOTE_PORT = 4;

    private Preference protocol;
    private Preference remotePort;
    private Preference vpnPortLocal;
    private Preference internetKillSwitch;
    private Preference tunnelMode;

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

        final CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();

        findPreference("split_tunnel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), ApplicationSettingsActivity.class));
                return true;
            }
        });

        findPreference("trusted_network").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(NetworkActivity.createIntent(getContext()));
                return true;
            }
        });

        // Internet Kill Switch
        internetKillSwitch = findPreference("internet_kill_switch");
        internetKillSwitch.setSummary(ResourceUtil.getStringFor(cypherpunkSetting.internetKillSwitch()));
        internetKillSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(InternetKillSwitchActivity.createIntent(getContext()),
                        REQUEST_CODE_INTERNET_KILL_SWITCH);
                return true;
            }
        });

        // Tunnel Mode
        tunnelMode = findPreference("tunnel_mode");
        tunnelMode.setSummary(ResourceUtil.getStringFor(cypherpunkSetting.tunnelMode()));
        tunnelMode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(TunnelModeActivity.createIntent(getContext()),
                        REQUEST_CODE_TUNNEL_MODE);
                return true;
            }
        });

        // Remote Port
        remotePort = findPreference("vpn_port_remote");
        remotePort.setSummary(ResourceUtil.getStringFor(cypherpunkSetting.remotePort()));
        remotePort.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(RemotePortActivity.createIntent(getContext()),
                        REQUEST_CODE_REMOTE_PORT);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            CypherpunkVpnStatus vpnStatus = new CypherpunkVpnStatus();
            if (vpnStatus.isConnected()) {
                SettingConnectDialogFragment dialogFragment = SettingConnectDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager());
            }
        }
        final CypherpunkSetting cypherpunkSetting = new CypherpunkSetting();
        switch (requestCode) {
            case REQUEST_CODE_INTERNET_KILL_SWITCH:
                internetKillSwitch.setSummary(ResourceUtil.getStringFor(cypherpunkSetting.internetKillSwitch()));
                break;
            case REQUEST_CODE_TUNNEL_MODE:
                tunnelMode.setSummary(ResourceUtil.getStringFor(cypherpunkSetting.tunnelMode()));
                break;
            case REQUEST_CODE_REMOTE_PORT:
                remotePort.setSummary(ResourceUtil.getStringFor(cypherpunkSetting.remotePort()));
                break;
        }
    }

    private ArrayList<SettingItem> getEncryptingSettingItemList(@ArrayRes int keyListRes, @ArrayRes int descriptionListRes) {
        String[] keyList = getResources().getStringArray(keyListRes);
        String[] descriptionList = new String[0];
        if (descriptionListRes != 0) {
            descriptionList = getResources().getStringArray(descriptionListRes);
        }

        ArrayList<SettingItem> list = new ArrayList<>();
        String cipher = null;
        String auth = null;
        String keylen = null;
        for (int i = 0; i < keyList.length; i++) {
            switch (keyList[i]) {
                case "setting_vpn_crypto_profile_none":
                    cipher = "setting_vpn_crypto_cipher_none";
                    auth = "setting_vpn_crypto_auth_sha256";
                    keylen = "setting_vpn_crypto_keylen_rsa4096";
                    break;
                case "setting_vpn_crypto_profile_default":
                    cipher = "setting_vpn_crypto_cipher_aes128cbc";
                    auth = "setting_vpn_crypto_auth_sha256";
                    keylen = "setting_vpn_crypto_keylen_rsa4096";
                    break;
                case "setting_vpn_crypto_profile_strong":
                    cipher = "setting_vpn_crypto_cipher_aes256cbc";
                    auth = "setting_vpn_crypto_auth_sha256";
                    keylen = "setting_vpn_crypto_keylen_rsa4096";
                    break;
                case "setting_vpn_crypto_profile_stealth":
                    cipher = "setting_vpn_crypto_cipher_aes128cbc";
                    auth = "setting_vpn_crypto_auth_sha256";
                    keylen = "setting_vpn_crypto_keylen_rsa4096";
            }
            list.add(new SettingItem(
                    keyList[i], ResourceUtil.getStringByKey(getContext(), keyList[i]),
                    descriptionListRes != 0 ? ResourceUtil.getStringByKey(getContext(), descriptionList[i]) : "",
                    ResourceUtil.getStringByKey(getContext(), cipher),
                    ResourceUtil.getStringByKey(getContext(), auth),
                    ResourceUtil.getStringByKey(getContext(), keylen)));
        }
        return list;
    }
}
