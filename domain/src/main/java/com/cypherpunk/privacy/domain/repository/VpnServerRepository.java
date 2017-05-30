package com.cypherpunk.privacy.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cypherpunk.privacy.datasource.vpn.Region;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * repository for vpn servers (Region)
 */
public interface VpnServerRepository {

    long count();

    @Nullable
    VpnServer fastest();

    @Nullable
    VpnServer find(@NonNull String regionId);

    @Nullable
    VpnServer findAuthorizedDefault(@NonNull String regionId);

    @NonNull
    List<VpnServer> findAllByRegion(@NonNull Region region);

    @NonNull
    List<VpnServer> findFavorites();

    @NonNull
    List<VpnServer> findRecent();

    void updateFavorite(@NonNull String regionId, boolean favorite);

    void updateLastConnectedDate(@NonNull String regionId, @NonNull Date date);

    void updateLatency(@NonNull String regionId, long latency);

    void updateServerList(@NonNull Map<String, RegionResult> regionMap);

    void addChangeListener(@NonNull ChangeListener listener);

    void removeChangeListener(@NonNull ChangeListener listener);

    interface ChangeListener {
        void onChanged();
    }
}
