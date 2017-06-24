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
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * fragment for select servers
 */
public class RegionFragment2 extends Fragment {

    public interface RegionFragmentListener {
        void onVpnServerSelected(@NonNull VpnServer vpnServer);
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

    @NonNull
    private Disposable disposable = Disposables.empty();
    private RegionAdapter2 adapter;

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

        adapter = new RegionAdapter2() {
            @Override
            protected void onItemClicked(@NonNull VpnServer vpnServer, boolean isCypherPlay) {
                tryChangeServer(vpnServer, isCypherPlay);
            }
        };
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

//        vpnServerRepository.addChangeListener(realmChangeListener);

        refreshRegionList();
//        syncServerList();
    }

//    private final VpnServerRepository.ChangeListener realmChangeListener = new VpnServerRepository.ChangeListener() {
//        @Override
//        public void onChanged() {
//            refreshRegionList();
//        }
//    };

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    public void refreshServerList() {
        syncServerList();
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
    }

    @NonNull
    private List<VpnServer> getOtherList(@NonNull Region region) {
        final List<VpnServer> regionList = vpnServerRepository.findAllByRegion(region);
        Collections.sort(regionList, new VpnServerComparator(Locale.getDefault()));
        return regionList;
    }

    private void syncServerList() {
        disposable = networkRepository.getAccountStatus()
                .flatMap(new Function<StatusResult, SingleSource<Map<String, RegionResult>>>() {
                    @Override
                    public SingleSource<Map<String, RegionResult>> apply(StatusResult result) throws Exception {
                        accountSetting.updateAccount(result.account);
                        return networkRepository.serverList(result.account.type());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Map<String, RegionResult>>() {
                    @Override
                    public void onSuccess(Map<String, RegionResult> result) {
                        vpnServerRepository.updateServerList(result);

                        // check if previously selected region is still available
                        final VpnServer vpnServer = vpnServerRepository.findAuthorizedDefault(vpnSetting.regionId());
                        if (vpnServer == null) {
                            tryChangeServer(vpnServerRepository.fastest(), false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void tryChangeServer(@Nullable VpnServer vpnServer, boolean isCypherPlay) {
        if (vpnServer != null) {
            vpnSetting.updateCypherplayEnabled(isCypherPlay);
            vpnSetting.updateRegionId(vpnServer.id());
            if (listener != null) {
                listener.onVpnServerSelected(vpnServer);
            }
        }
    }
}
