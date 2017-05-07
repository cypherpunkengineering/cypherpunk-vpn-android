package com.cypherpunk.privacy.domain.service;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.data.api.json.RegionResult;
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
            final String id = regionResult.getId();

            updateRegionIdList.add(id);

            final Region region = realm.where(Region.class)
                    .equalTo("id", id)
                    .findFirst();

            if (region != null) {
                region.setRegion(regionResult.getRegion());
                region.setCountry(regionResult.getCountry());
                region.setRegionName(regionResult.getName());
                region.setLevel(regionResult.getLevel());
                region.setAuthorized(regionResult.isAuthorized());
                region.setOvHostname(regionResult.getOvHostname());
                region.setOvDefault(regionResult.getOvDefault());
                region.setOvNone(regionResult.getOvNone());
                region.setOvStrong(regionResult.getOvStrong());
                region.setOvStealth(regionResult.getOvStealth());
            } else {
                realm.copyToRealm(new Region(
                        regionResult.getId(),
                        regionResult.getRegion(),
                        regionResult.getCountry(),
                        regionResult.getName(),
                        regionResult.getLevel(),
                        regionResult.isAuthorized(),
                        regionResult.getOvHostname(),
                        regionResult.getOvDefault(),
                        regionResult.getOvNone(),
                        regionResult.getOvStrong(),
                        regionResult.getOvStealth()));
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
                    .equalTo("id", regionResult.getId())
                    .findFirst();
            ServerPingerThinger.pingLocation(region);
        }
    }
}
