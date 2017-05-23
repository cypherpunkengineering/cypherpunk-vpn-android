package com.cypherpunk.privacy.domain.service;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.model.Region;
import com.cypherpunk.privacy.vpn.ServerPingerThinger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Domain service for servers
 */
public class ServerService {

    @NonNull
    private final Realm realm;

    public ServerService(@NonNull Realm realm) {
        this.realm = realm;
    }

    public void updateServerList(@NonNull Map<String, RegionResult> result) {
        realm.beginTransaction();

        final List<String> updateRegionIdList = new ArrayList<>();
        for (RegionResult regionResult : result.values()) {
            final String id = regionResult.id;

            updateRegionIdList.add(id);

            final Region region = realm.where(Region.class)
                    .equalTo("id", id)
                    .findFirst();

            if (region != null) {
                region.setRegion(regionResult.region);
                region.setCountry(regionResult.country);
                region.setRegionName(regionResult.name);
                region.setLevel(regionResult.level);
                region.setAuthorized(regionResult.authorized);
                region.setOvHostname(regionResult.ovHostname);
                region.setOvDefault(regionResult.ovDefault);
                region.setOvNone(regionResult.ovNone);
                region.setOvStrong(regionResult.ovStrong);
                region.setOvStealth(regionResult.ovStealth);
            } else {
                realm.copyToRealm(new Region(
                        regionResult.id,
                        regionResult.region,
                        regionResult.country,
                        regionResult.name,
                        regionResult.level,
                        regionResult.authorized,
                        regionResult.ovHostname,
                        regionResult.ovDefault,
                        regionResult.ovNone,
                        regionResult.ovStrong,
                        regionResult.ovStealth));
            }
        }

        final RealmResults<Region> oldRegions = realm.where(Region.class)
                .not()
                .beginGroup()
                .in("id", updateRegionIdList.toArray(new String[updateRegionIdList.size()]))
                .endGroup()
                .findAll();
        oldRegions.deleteAllFromRealm();

        realm.commitTransaction();

        // after realm db is updated above, start pinging new location data
        for (RegionResult regionResult : result.values()) {
            final Region region = realm.where(Region.class)
                    .equalTo("id", regionResult.id)
                    .findFirst();
            ServerPingerThinger.pingLocation(region);
        }
    }
}
