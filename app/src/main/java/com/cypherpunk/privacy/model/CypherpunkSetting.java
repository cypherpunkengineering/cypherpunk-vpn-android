package com.cypherpunk.privacy.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.InternetKillSwitch;
import com.cypherpunk.privacy.domain.model.RemotePort;
import com.cypherpunk.privacy.domain.model.TunnelMode;
import com.cypherpunk.privacy.domain.model.VpnSetting;

import java.util.Arrays;
import java.util.List;

public class CypherpunkSetting implements VpnSetting {

    private static final String PREF_NAME = "cypherpunk_setting";

    private final SharedPreferences pref;
    private final String KEY_AUTO_SECURE_UNTRUSTED;
    private final String KEY_AUTO_SECURE_OTHER;
    private final String KEY_AUTO_CONNECT;
    private final String KEY_BLOCK_MALWARE;
    private final String KEY_BLOCK_ADS;
    private final String KEY_INTERNET_KILL_SWITCH;
    private final String KEY_TUNNEL_MODE;
    private final String KEY_REMOTE_PORT_TYPE;
    private final String KEY_REMOTE_PORT_PORT;
    private final String KEY_EXCEPT_APP_LIST;
    private final String KEY_ALLOW_LAN_TRAFFIC;
    private final String KEY_REGION_ID;
    private final String KEY_CYPHERPLAY;
    private final String KEY_ANALYTICS;

    private CypherpunkSetting(@NonNull Context c) {
        pref = c.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        KEY_AUTO_SECURE_UNTRUSTED = c.getString(R.string.setting_preference_key_auto_secure_untrusted);
        KEY_AUTO_SECURE_OTHER = c.getString(R.string.setting_preference_key_auto_secure_other);
        KEY_AUTO_CONNECT = c.getString(R.string.setting_preference_key_auto_connect);
        KEY_BLOCK_MALWARE = c.getString(R.string.setting_preference_key_block_malware);
        KEY_BLOCK_ADS = c.getString(R.string.setting_preference_key_block_ads);
        KEY_INTERNET_KILL_SWITCH = c.getString(R.string.setting_preference_key_internet_kill_switch);
        KEY_TUNNEL_MODE = c.getString(R.string.setting_preference_key_tunnel_mode);
        KEY_REMOTE_PORT_TYPE = c.getString(R.string.setting_preference_key_remote_port_type);
        KEY_REMOTE_PORT_PORT = c.getString(R.string.setting_preference_key_remote_port_port);
        KEY_EXCEPT_APP_LIST = c.getString(R.string.setting_preference_key_except_app_list);
        KEY_ALLOW_LAN_TRAFFIC = c.getString(R.string.setting_preference_key_allow_lan_traffic);
        KEY_REGION_ID = c.getString(R.string.setting_preference_key_region_id);
        KEY_CYPHERPLAY = c.getString(R.string.setting_preference_key_cypherplay);
        KEY_ANALYTICS = c.getString(R.string.setting_preference_key_analytics);
    }

    //

    @Override
    public boolean isAutoSecureUntrusted() {
        return pref.getBoolean(KEY_AUTO_SECURE_UNTRUSTED, false);
    }

    @Override
    public void updateAutoSecureUntrusted(boolean b) {
        pref.edit().putBoolean(KEY_AUTO_SECURE_UNTRUSTED, b).apply();
    }

    //

    @Override
    public boolean isAutoSecureOther() {
        return pref.getBoolean(KEY_AUTO_SECURE_OTHER, false);
    }

    @Override
    public void updateAutoSecureOther(boolean b) {
        pref.edit().putBoolean(KEY_AUTO_SECURE_OTHER, b).apply();
    }

    //

    @Override
    public boolean isAutoConnect() {
        return pref.getBoolean(KEY_AUTO_CONNECT, false);
    }

    @Override
    public boolean isBlockMalware() {
        return pref.getBoolean(KEY_BLOCK_MALWARE, false);
    }

    @Override
    public boolean isBlockAds() {
        return pref.getBoolean(KEY_BLOCK_ADS, false);
    }

    //

    @NonNull
    @Override
    public InternetKillSwitch internetKillSwitch() {
        return InternetKillSwitch.find(pref.getString(KEY_INTERNET_KILL_SWITCH, null));
    }

    @Override
    public void updateInternetKillSwitch(@NonNull InternetKillSwitch internetKillSwitch) {
        pref.edit().putString(KEY_INTERNET_KILL_SWITCH, internetKillSwitch.value()).apply();
    }

    @NonNull
    @Override
    public TunnelMode tunnelMode() {
        return TunnelMode.find(pref.getString(KEY_TUNNEL_MODE, null));
    }

    @Override
    public void updateTunnelMode(@NonNull TunnelMode tunnelMode) {
        pref.edit().putString(KEY_TUNNEL_MODE, tunnelMode.name()).apply();
    }

    @NonNull
    @Override
    public RemotePort remotePort() {
        final RemotePort.Type type = RemotePort.Type.find(pref.getString(KEY_REMOTE_PORT_TYPE, null));
        final RemotePort.Port port = RemotePort.Port.find(pref.getInt(KEY_REMOTE_PORT_PORT, 0));
        return RemotePort.create(type, port);
    }

    @Override
    public void updateRemotePort(@NonNull RemotePort remotePort) {
        pref.edit()
                .putString(KEY_REMOTE_PORT_TYPE, remotePort.type().name())
                .putInt(KEY_REMOTE_PORT_PORT, remotePort.port().value())
                .apply();
    }

    @NonNull
    @Override
    public List<String> exceptAppList() {
        return Arrays.asList(pref.getString(KEY_EXCEPT_APP_LIST, "").split(","));
    }

    @Override
    public void updateExceptAppList(@NonNull List<String> exceptAppList) {
        pref.edit().putString(KEY_EXCEPT_APP_LIST, TextUtils.join(",", exceptAppList)).apply();
    }

    @Override
    public boolean allowLanTraffic() {
        return pref.getBoolean(KEY_ALLOW_LAN_TRAFFIC, true);
    }

    @NonNull
    @Override
    public String regionId() {
        return pref.getString(KEY_REGION_ID, "");
    }

    @Override
    public void updateRegionId(@NonNull String regionId) {
        pref.edit().putString(KEY_REGION_ID, regionId).apply();
    }

    @Override
    public boolean isCypherplayEnabled() {
        return pref.getBoolean(KEY_CYPHERPLAY, false);
    }

    @Override
    public void updateCypherplayEnabled(boolean b) {
        pref.edit().putBoolean(KEY_CYPHERPLAY, b).apply();
    }

    @Override
    public boolean isAnalyticsEnabled() {
        return pref.getBoolean(KEY_ANALYTICS, false);
    }

    @Override
    public void updateAnalyticsEnabled(boolean b) {
        pref.edit().putBoolean(KEY_ANALYTICS, b).apply();
    }

    //
    //
    //

    private static CypherpunkSetting instance;

    public static void init(@NonNull Context context) {
        instance = new CypherpunkSetting(context);
    }

    @NonNull
    public static VpnSetting vpnSetting() {
        if (instance == null) {
            throw new IllegalStateException("call init() first");
        }
        return instance;
    }
}
