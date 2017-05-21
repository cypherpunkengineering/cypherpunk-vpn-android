package com.cypherpunk.privacy.domain.model.vpn;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cypherpunk.privacy.BuildConfig;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.vpn.ServerPingerThinger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * implementation of RegionRepository with Realm
 */
public class RealmVpnServerRepository implements VpnServerRepository {

    @NonNull
    private final RealmConfiguration conf;

    public RealmVpnServerRepository() {
        final RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        if (BuildConfig.DEBUG) {
            builder.deleteRealmIfMigrationNeeded();
        }
        conf = builder.build();
    }

    @NonNull
    private Realm realmInstance() {
        return Realm.getInstance(conf);
    }

    @Override
    public long count() {
        final Realm realm = realmInstance();
        final long count = realm.where(RealmVpnServer.class).count();
        realm.close();
        return count;
    }

    @Override
    public VpnServer fastest() {
        final Realm realm = realmInstance();

        try {
            // first get locations which have valid latency data
            RealmResults<RealmVpnServer> results = realm.where(RealmVpnServer.class)
                    .equalTo("authorized", true)
                    .contains("ovDefault", ".")
                    .notEqualTo("level", "developer")
                    .notEqualTo("latency", -1)
                    .findAllSorted("latency", Sort.ASCENDING);

            if (results.size() > 0) {
                return results.get(0);
            }

            // if no latency data, just return any location
            results = realm.where(RealmVpnServer.class)
                    .equalTo("authorized", true)
                    .contains("ovDefault", ".")
                    .notEqualTo("level", "developer")
                    .findAllSorted("latency", Sort.ASCENDING);

            if (results.size() > 0) {
                return results.get(0);
            }

            return null;

        } finally {
            realm.close();
        }
    }

    @Nullable
    @Override
    public VpnServer find(@NonNull String regionId) {
        final Realm realm = realmInstance();
        try {
            return realm.where(RealmVpnServer.class)
                    .equalTo("id", regionId)
                    .findFirst();
        } finally {
            realm.close();
        }
    }

    @Nullable
    @Override
    public VpnServer findAuthorizedDefault(@NonNull String regionId) {
        final Realm realm = realmInstance();
        try {
            return realm.where(RealmVpnServer.class)
                    .equalTo("id", regionId)
                    .equalTo("authorized", true)
                    .contains("ovDefault", ".")
                    .findFirst();
        } finally {
            realm.close();
        }
    }

    @NonNull
    @Override
    public List<VpnServer> findAllByRegion(@NonNull String region) {
        final Realm realm = realmInstance();
        final RealmResults<RealmVpnServer> results = realm.where(RealmVpnServer.class)
                .equalTo("region", region)
                .findAll();
        realm.close();
        return new ArrayList<VpnServer>(results);
    }

    @NonNull
    @Override
    public List<VpnServer> findFavorites() {
        final Realm realm = realmInstance();
        final RealmResults<RealmVpnServer> results = realm.where(RealmVpnServer.class)
                .equalTo("favorited", true)
                .findAllSorted("regionName", Sort.ASCENDING);
        realm.close();
        return new ArrayList<VpnServer>(results);
    }

    @NonNull
    @Override
    public List<VpnServer> findRecent() {
        final Realm realm = realmInstance();
        final RealmResults<RealmVpnServer> results = realm.where(RealmVpnServer.class)
                .equalTo("favorited", false)
                .notEqualTo("lastConnectedDate", new Date(0))
                .findAllSorted("lastConnectedDate", Sort.DESCENDING);
        realm.close();
        final List<VpnServer> vpnServers = new ArrayList<>();
        for (int i = 0, max = Math.min(3, results.size()); i < max; i++) {
            vpnServers.add(results.get(i));
        }
        return vpnServers;
    }

    @Override
    public void updateFavorite(@NonNull String regionId, boolean favorite) {
        final Realm realm = realmInstance();
        final RealmVpnServer region = realm.where(RealmVpnServer.class)
                .equalTo("id", regionId)
                .findFirst();
        if (region != null && region.favorite() != favorite) {
            realm.beginTransaction();
            region.setFavorited(favorite);
            realm.commitTransaction();
        }
        realm.close();
    }

    @Override
    public void updateLastConnectedDate(@NonNull String regionId, @NonNull Date date) {
        final Realm realm = realmInstance();
        final RealmVpnServer region = realm.where(RealmVpnServer.class)
                .equalTo("id", regionId)
                .findFirst();
        if (region != null) {
            realm.beginTransaction();
            region.setLastConnectedDate(date);
            realm.commitTransaction();
        }
        realm.close();
    }

    @Override
    public void updateLatency(@NonNull String regionId, long latency) {
        final Realm realm = realmInstance();
        realm.beginTransaction();
        final RealmVpnServer location = realm.where(RealmVpnServer.class)
                .equalTo("id", regionId)
                .findFirst();
        location.setLatency(latency);
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void updateServerList(@NonNull Map<String, RegionResult> result) {
        if (result.isEmpty()) {
            return;
        }

        final Realm realm = realmInstance();
        realm.beginTransaction();

        final List<String> regionIdList = new ArrayList<>();

        for (RegionResult regionResult : result.values()) {
            final String id = regionResult.id;

            regionIdList.add(id);

            final RealmVpnServer region = realm.where(RealmVpnServer.class)
                    .equalTo("id", id)
                    .findFirst();

            // FIXME
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
                realm.copyToRealm(new RealmVpnServer(
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

        // delete old regions
        realm.where(RealmVpnServer.class)
                .not()
                .beginGroup()
                .in("id", regionIdList.toArray(new String[regionIdList.size()]))
                .endGroup()
                .findAll()
                .deleteAllFromRealm();

        realm.commitTransaction();
        realm.close();

        // after realm db is updated above, start pinging new location data
        for (String regionId : regionIdList) {
            final RealmVpnServer region = realm.where(RealmVpnServer.class)
                    .equalTo("id", regionId)
                    .findFirst();
            ServerPingerThinger.pingLocation(region, this);
        }
    }

    @Override
    public void addChangeListener(@NonNull final ChangeListener listener) {
        if (realmChangeListeners.containsKey(listener)) {
            return;
        }
        if (realmForChange == null) {
            realmForChange = realmInstance();
        }
        final RealmChangeListener<Realm> l = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                listener.onChanged();
            }
        };
        realmChangeListeners.put(listener, l);
        realmForChange.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(@NonNull ChangeListener listener) {
        final RealmChangeListener<Realm> l = realmChangeListeners.remove(listener);
        if (realmForChange != null) {
            if (l != null) {
                realmForChange.removeChangeListener(l);
            }
            if (realmChangeListeners.isEmpty()) {
                realmForChange.close();
                realmForChange = null;
            }
        }
    }

    @Nullable
    private Realm realmForChange;
    private final Map<ChangeListener, RealmChangeListener<Realm>> realmChangeListeners = new HashMap<>();
}
