package com.cypherpunk.privacy.datasource.vpn;

import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.Locale;

/**
 * comparator for VpnServer
 */
public class VpnServerComparator implements Comparator<VpnServer> {

    @NonNull
    private final Locale locale;

    public VpnServerComparator(@NonNull Locale locale) {
        this.locale = locale;
    }

    @Override
    public int compare(VpnServer vpnServer1, VpnServer vpnServer2) {
        final Locale countryLocale1 = new Locale(locale.getLanguage(), vpnServer1.country());
        final String countryName1 = countryLocale1.getDisplayCountry(locale);

        final Locale countryLocale2 = new Locale(locale.getLanguage(), vpnServer2.country());
        final String countryName2 = countryLocale2.getDisplayCountry(locale);

        final int result = countryName1.compareTo(countryName2);
        if (result == 0) {
            return vpnServer1.name().compareTo(vpnServer2.name());
        }
        return result;
    }
}
