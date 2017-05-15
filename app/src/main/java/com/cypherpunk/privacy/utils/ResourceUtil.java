package com.cypherpunk.privacy.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.model.CypherpunkSetting;

import static com.os.operando.garum.utils.Cache.getContext;

public class ResourceUtil {

    public static int getFlagDrawableByKey(Context context, String key) {
        String packageName = getContext().getPackageName();
        return context.getResources().getIdentifier("flag_" + key, "drawable", packageName);
    }

    public static String getStringByKey(Context context, String key) {
        String packageName = getContext().getPackageName();
        int id = context.getResources().getIdentifier(key, "string", packageName);
        String str;
        try {
            str = context.getResources().getString(id);
        } catch (Exception NotFoundException) {
            str = key;
        }
        return str;
    }

    @StringRes
    public static int getStringFor(@NonNull CypherpunkSetting.InternetKillSwitch internetKillSwitch) {
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

    public static String getStringFor(@NonNull CypherpunkSetting.RemotePort remotePort) {
        return remotePort.category.name() + " " + remotePort.port.value();
    }

    @StringRes
    public static int getStringFor(@NonNull CypherpunkSetting.TunnelMode mode) {
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

    @StringRes
    public static int getTitleFor(@NonNull CypherpunkSetting.TunnelMode mode) {
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

    @StringRes
    public static int getSummaryFor(@NonNull CypherpunkSetting.TunnelMode mode) {
        switch (mode) {
            case RECOMMENDED:
                return R.string.tunnel_mode_recommended_summary;
            case MAX_SPEED:
                return R.string.tunnel_mode_max_speed_summary;
            case MAX_PRIVACY:
                return R.string.tunnel_mode_max_privacy_summary;
            case MAX_STEALTH:
                return R.string.tunnel_mode_max_stealth_summary;
            default:
                throw new IllegalArgumentException();
        }
    }
}
