package com.cypherpunk.privacy.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.vpn.InternetKillSwitch;
import com.cypherpunk.privacy.domain.model.vpn.RemotePort;
import com.cypherpunk.privacy.domain.model.vpn.TunnelMode;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.vpn.CypherpunkVpnStatus;

import javax.inject.Inject;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_INTERNET_KILL_SWITCH = 2;
    private static final int REQUEST_CODE_TUNNEL_MODE = 3;
    private static final int REQUEST_CODE_REMOTE_PORT = 4;

    private Preference remotePort;
    private Preference internetKillSwitch;
    private Preference tunnelMode;

    @Inject
    VpnSetting vpnSetting;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        setDivider(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.divider)));
        setDividerHeight(getContext().getResources().getDimensionPixelSize(R.dimen.divider));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName("cypherpunk_setting");

        addPreferencesFromResource(R.xml.preference_settings);

        findPreference(getString(R.string.setting_preference_network))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(NetworkActivity.createIntent(getContext()));
                        return true;
                    }
                });

        // Internet Kill Switch
        internetKillSwitch = findPreference(getString(R.string.setting_preference_internet_kill_switch));
        internetKillSwitch.setSummary(getStringFor(vpnSetting.internetKillSwitch()));
        internetKillSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(InternetKillSwitchActivity.createIntent(getContext()),
                        REQUEST_CODE_INTERNET_KILL_SWITCH);
                return true;
            }
        });

        // Tunnel Mode
        tunnelMode = findPreference(getString(R.string.setting_preference_tunnel_mode));
        tunnelMode.setSummary(getStringFor(vpnSetting.tunnelMode()));
        tunnelMode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(TunnelModeActivity.createIntent(getContext()),
                        REQUEST_CODE_TUNNEL_MODE);
                return true;
            }
        });

        // Remote Port
        remotePort = findPreference(getString(R.string.setting_preference_remote_port));
        remotePort.setSummary(getStringFor(vpnSetting.remotePort()));
        remotePort.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(RemotePortActivity.createIntent(getContext()),
                        REQUEST_CODE_REMOTE_PORT);
                return true;
            }
        });

        findPreference(getString(R.string.setting_preference_split_tunnel))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getContext(), SplitTunnelActivity.class));
                        return true;
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_INTERNET_KILL_SWITCH:
                    internetKillSwitch.setSummary(getStringFor(vpnSetting.internetKillSwitch()));
                    break;
                case REQUEST_CODE_TUNNEL_MODE:
                    tunnelMode.setSummary(getStringFor(vpnSetting.tunnelMode()));
                    break;
                case REQUEST_CODE_REMOTE_PORT:
                    remotePort.setSummary(getStringFor(vpnSetting.remotePort()));
                    break;
            }
            final CypherpunkVpnStatus vpnStatus = new CypherpunkVpnStatus();
            if (vpnStatus.isConnected()) {
                SettingConnectDialogFragment.newInstance().show(getFragmentManager());
            }
        }
    }

    @StringRes
    public static int getStringFor(@NonNull InternetKillSwitch internetKillSwitch) {
        switch (internetKillSwitch) {
            case AUTOMATIC:
                return R.string.internet_kill_switch_automatic_title;
            case ALWAYS_ON:
                return R.string.internet_kill_switch_always_on_title;
            case OFF:
                return R.string.internet_kill_switch_off_title;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static String getStringFor(@NonNull RemotePort remotePort) {
        return remotePort.type().name() + " " + remotePort.port().value();
    }

    @StringRes
    public static int getStringFor(@NonNull TunnelMode mode) {
        switch (mode) {
            case RECOMMENDED:
                return R.string.tunnel_mode_recommended_title;
            case MAX_SPEED:
                return R.string.tunnel_mode_max_speed_title;
            case MAX_PRIVACY:
                return R.string.tunnel_mode_max_privacy_title;
            case MAX_STEALTH:
                return R.string.tunnel_mode_max_stealth_title;
            default:
                throw new IllegalArgumentException();
        }
    }
}
