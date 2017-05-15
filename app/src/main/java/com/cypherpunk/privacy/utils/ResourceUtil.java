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
}
