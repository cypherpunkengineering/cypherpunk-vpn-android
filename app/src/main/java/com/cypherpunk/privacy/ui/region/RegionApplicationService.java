package com.cypherpunk.privacy.ui.region;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.datasource.account.Account;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.vpn.ServerPingerThinger;
import com.cypherpunk.privacy.vpn.VpnManager;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class RegionApplicationService {

    @NonNull
    public static Completable updateServer(@NonNull NetworkRepository networkRepository,
                                           @NonNull Account.Type type,
                                           @NonNull final VpnServerRepository vpnServerRepository,
                                           @NonNull final VpnManager vpnManager) {

        return networkRepository.serverList(type)
                .doOnSuccess(new Consumer<Map<String, RegionResult>>() {
                    @Override
                    public void accept(Map<String, RegionResult> result) throws Exception {
                        final List<String> regionIdList = vpnServerRepository.updateServerList(result);

                        // after sever db is updated, start pinging new location data
                        for (String regionId : regionIdList) {
                            final VpnServer vpnServer = vpnServerRepository.find(regionId);
                            if (vpnServer != null) {
                                if (vpnServer.isPingable()) {
                                    new ServerPingerThinger(vpnServer, vpnManager, vpnServerRepository)
                                            .start();
                                } else {
                                    Timber.d("Skipping ping for location " + vpnServer.id());
                                    // update latency to -1 when locations become unavailable
                                    vpnServerRepository.updateLatency(vpnServer.id(), -1);
                                }
                            }
                        }
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        Timber.e(e);
                    }
                })
                .toCompletable();
    }
}
