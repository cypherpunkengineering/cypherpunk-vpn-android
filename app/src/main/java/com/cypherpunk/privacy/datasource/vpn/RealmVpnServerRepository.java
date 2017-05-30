package com.cypherpunk.privacy.datasource.vpn;

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

import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_AUTHORIZED;
import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_DATE;
import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_FAVORITE;
import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_ID;
import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_LATENCY;
import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_LEVEL;
import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_NAME;
import static com.cypherpunk.privacy.datasource.vpn.RealmVpnServer.KEY_OV_DEFAULT;

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
                    .equalTo(KEY_AUTHORIZED, true)
                    .contains(KEY_OV_DEFAULT, ".")
                    .notEqualTo(KEY_LEVEL, Level.DEVELOPER.value())
                    .notEqualTo(KEY_LATENCY, -1)
                    .findAllSorted(KEY_LATENCY, Sort.ASCENDING);

            if (results.size() > 0) {
                return results.get(0);
            }

            // if no latency data, just return any location
            results = realm.where(RealmVpnServer.class)
                    .equalTo(KEY_AUTHORIZED, true)
                    .contains(KEY_OV_DEFAULT, ".")
                    .notEqualTo(KEY_LEVEL, Level.DEVELOPER.value())
                    .findAllSorted(KEY_LATENCY, Sort.ASCENDING);

            if (results.size() > 0) {
                return realm.copyFromRealm(results.get(0));
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
            final RealmVpnServer vpnServer = realm.where(RealmVpnServer.class)
                    .equalTo(KEY_ID, regionId)
                    .findFirst();
            return vpnServer != null ? realm.copyFromRealm(vpnServer) : null;
        } finally {
            realm.close();
        }
    }

    @Nullable
    @Override
    public VpnServer findAuthorizedDefault(@NonNull String regionId) {
        final Realm realm = realmInstance();
        try {
            final RealmVpnServer vpnServer = realm.where(RealmVpnServer.class)
                    .equalTo(KEY_ID, regionId)
                    .equalTo(KEY_AUTHORIZED, true)
                    .contains(KEY_OV_DEFAULT, ".")
                    .findFirst();
            return vpnServer != null ? realm.copyFromRealm(vpnServer) : null;
        } finally {
            realm.close();
        }
    }

    @NonNull
    @Override
    public List<VpnServer> findAllByRegion(@NonNull Region region) {
        final Realm realm = realmInstance();
        try {
            final RealmResults<RealmVpnServer> results = realm.where(RealmVpnServer.class)
                    .equalTo(RealmVpnServer.KEY_REGION, region.value())
                    .findAll();
            return new ArrayList<VpnServer>(realm.copyFromRealm(results));
        } finally {
            realm.close();
        }
    }

    @NonNull
    @Override
    public List<VpnServer> findFavorites() {
        final Realm realm = realmInstance();
        try {
            final RealmResults<RealmVpnServer> results = realm.where(RealmVpnServer.class)
                    .equalTo(KEY_FAVORITE, true)
                    .findAllSorted(KEY_NAME, Sort.ASCENDING);
            return new ArrayList<VpnServer>(realm.copyFromRealm(results));
        } finally {
            realm.close();
        }
    }

    @NonNull
    @Override
    public List<VpnServer> findRecent() {
        final Realm realm = realmInstance();
        try {
            final RealmResults<RealmVpnServer> results = realm.where(RealmVpnServer.class)
                    .equalTo(KEY_FAVORITE, false)
                    .notEqualTo(KEY_DATE, new Date(0))
                    .findAllSorted(KEY_DATE, Sort.DESCENDING);
            final List<VpnServer> vpnServers = new ArrayList<>();
            for (int i = 0, max = Math.min(3, results.size()); i < max; i++) {
                vpnServers.add(realm.copyFromRealm(results.get(i)));
            }
            return vpnServers;
        } finally {
            realm.close();
        }
    }

    @Override
    public void updateFavorite(@NonNull String regionId, boolean favorite) {
        final Realm realm = realmInstance();
        final RealmVpnServer region = realm.where(RealmVpnServer.class)
                .equalTo(KEY_ID, regionId)
                .findFirst();
        if (region != null && region.favorite() != favorite) {
            realm.beginTransaction();
            region.setFavorite(favorite);
            realm.commitTransaction();
        }
        realm.close();
    }

    @Override
    public void updateLastConnectedDate(@NonNull String regionId, @NonNull Date date) {
        final Realm realm = realmInstance();
        final RealmVpnServer region = realm.where(RealmVpnServer.class)
                .equalTo(KEY_ID, regionId)
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
                .equalTo(KEY_ID, regionId)
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
                    .equalTo(KEY_ID, id)
                    .findFirst();

            if (region != null) {
                region.update(regionResult.name,
                        regionResult.country,
                        regionResult.region,
                        regionResult.level,
                        regionResult.authorized,
                        regionResult.ovHostname,
                        regionResult.ovDefault,
                        regionResult.ovNone,
                        regionResult.ovStrong,
                        regionResult.ovStealth);
            } else {
                realm.copyToRealm(new RealmVpnServer(
                        regionResult.id,
                        regionResult.name,
                        regionResult.country,
                        regionResult.region,
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
                .in(KEY_ID, regionIdList.toArray(new String[regionIdList.size()]))
                .endGroup()
                .findAll()
                .deleteAllFromRealm();

        realm.commitTransaction();
        realm.close();

        // after realm db is updated above, start pinging new location data
        for (String regionId : regionIdList) {
            final RealmVpnServer region = realm.where(RealmVpnServer.class)
                    .equalTo(KEY_ID, regionId)
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
