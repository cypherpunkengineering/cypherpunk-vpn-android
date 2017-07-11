package com.cypherpunk.privacy.ui.region;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.Region;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.datasource.vpn.VpnServerComparator;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.vpn.VpnManager;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * fragment for select servers
 */
public class RegionFragment extends Fragment {

    public interface RegionFragmentListener {
        void onVpnServerSelected(@NonNull VpnServer vpnServer, boolean isUserSelected);

        void onVpnServerListChanged(@NonNull List<VpnServer> vpnServerList);
    }

    @Nullable
    private RegionFragmentListener listener;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    NetworkRepository networkRepository;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    @Inject
    VpnManager vpnManager;

    @NonNull
    private Disposable disposable = Disposables.empty();
    private RegionAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegionFragmentListener) {
            listener = (RegionFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_region, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
//        vpnServerRepository.removeChangeListener(realmChangeListener);
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((CypherpunkApplication) getActivity().getApplication()).getAppComponent()
                .inject(this);

        adapter = new RegionAdapter() {
            @Override
            protected void onItemClicked(@NonNull VpnServer vpnServer, boolean isCypherPlay) {
                tryChangeServer(vpnServer, isCypherPlay, true);
            }
        };
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        refreshRegionList();
        syncServerList();
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    private void refreshRegionList() {
        adapter.addRegionList(vpnServerRepository.fastest(),
                getOtherList(Region.DEVELOP),
                getOtherList(Region.NORTH_AMERICA),
                getOtherList(Region.SOUTH_AMERICA),
                getOtherList(Region.CR),
                getOtherList(Region.EUROPE),
                getOtherList(Region.ME),
                getOtherList(Region.AF),
                getOtherList(Region.ASIA),
                getOtherList(Region.OCEANIA_PACIFIC));

        if (listener != null) {
            listener.onVpnServerListChanged(adapter.getVpnServers());
        }
    }

    @NonNull
    private List<VpnServer> getOtherList(@NonNull Region region) {
        final List<VpnServer> regionList = vpnServerRepository.findAllByRegion(region);
        Collections.sort(regionList, new VpnServerComparator(Locale.getDefault()));
        return regionList;
    }

    private void syncServerList() {
        disposable = networkRepository.getAccountStatus()
                .flatMapCompletable(new Function<StatusResult, Completable>() {
                    @Override
                    public Completable apply(StatusResult result) throws Exception {
                        accountSetting.updateAccount(result.account);
                        return RegionApplicationService.updateServer(networkRepository,
                                result.account.type(),
                                vpnServerRepository,
                                vpnManager);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        refreshRegionList();
                        // check if previously selected region is still available
                        final VpnServer vpnServer = vpnServerRepository.findAuthorizedDefault(vpnSetting.regionId());
                        if (vpnServer == null) {
                            tryChangeServer(vpnServerRepository.fastest(), false, false);
                        }
                    }
                });
    }

    private void tryChangeServer(@Nullable VpnServer vpnServer, boolean isCypherPlay, boolean isUserSelected) {
        if (vpnServer != null) {
            vpnSetting.updateCypherplayEnabled(isCypherPlay);
            vpnSetting.updateRegionId(vpnServer.id());
            if (listener != null) {
                listener.onVpnServerSelected(vpnServer, isUserSelected);
            }
        }
    }
}
