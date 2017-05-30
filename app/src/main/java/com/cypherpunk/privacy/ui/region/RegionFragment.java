package com.cypherpunk.privacy.ui.region;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.datasource.vpn.Region;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;
import com.cypherpunk.privacy.datasource.vpn.VpnServerComparator;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.ui.common.RegionBadgeView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RegionFragment extends Fragment {

    public interface RegionFragmentListener {
        void toggleBottomSheetState();

        void onSelectedRegionChanged(@NonNull String regionName, @DrawableRes int nationalFlagResId, boolean connectNow);
    }

    @NonNull
    public static RegionFragment newInstance() {
        return new RegionFragment();
    }

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    NetworkRepository networkRepository;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    @Nullable
    private RegionFragmentListener listener;
    @NonNull
    private Disposable disposable = Disposables.empty();
    private RegionAdapter adapter;
    private Unbinder unbinder;

    @BindView(R.id.region_container)
    View regionContainer;

    @BindView(R.id.national_flag)
    ImageView flagView;

    @BindView(R.id.region_name)
    TextView nameView;

    @BindView(R.id.region_tag)
    RegionBadgeView tagView;

    @BindView(R.id.allow)
    ImageView allowView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

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

        if (getResources().getBoolean(R.bool.is_tablet)) {
            regionContainer.setVisibility(View.GONE);
        }

        return view;
    }

    @OnClick(R.id.region_container)
    void onRegionContainerClicked() {
        if (listener != null) {
            listener.toggleBottomSheetState();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((CypherpunkApplication) getActivity().getApplication()).getAppComponent().inject(this);

        final String id = vpnSetting.regionId();
        // if no location set, select fastest region using available data
        if (TextUtils.isEmpty(id)) {
            tryChangeServer(vpnServerRepository.fastest(), false, false);
        } else {
            final VpnServer vpnServer = vpnServerRepository.find(id);
            if (vpnServer != null) {
                onVpnServerChanged(vpnServer);
            }
        }

        adapter = new RegionAdapter(getContext(), vpnSetting) {
            @Override
            protected void onFavoriteButtonClicked(@NonNull String id, boolean favorite) {
                vpnServerRepository.updateFavorite(id, favorite);
            }

            @Override
            protected void onItemClicked(@NonNull String id) {
                tryChangeServer(vpnServerRepository.find(id), false, true);
            }

            @Override
            protected void onCypherplayClicked() {
                tryChangeServer(vpnServerRepository.fastest(), true, true);
            }

            @Override
            protected void onFastestClicked() {
                tryChangeServer(vpnServerRepository.fastest(), false, true);
            }
        };
        recyclerView.setAdapter(adapter);

        vpnServerRepository.addChangeListener(realmChangeListener);

        refreshRegionList();
        syncServerList();
    }

    private final VpnServerRepository.ChangeListener realmChangeListener = new VpnServerRepository.ChangeListener() {
        @Override
        public void onChanged() {
            refreshRegionList();
        }
    };

    @Override
    public void onDestroyView() {
        vpnServerRepository.removeChangeListener(realmChangeListener);
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    public void toggleAllowIcon(boolean more) {
        allowView.setImageResource(more ? R.drawable.ic_expand_more_vector : R.drawable.ic_expand_less_vector);
    }

    public void refreshServerList() {
        syncServerList();
    }

    private void refreshRegionList() {
        adapter.addRegionList(
                vpnServerRepository.findFavorites(),
                vpnServerRepository.findRecent(),
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
                            tryChangeServer(vpnServerRepository.fastest(), false, false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void tryChangeServer(@Nullable VpnServer vpnServer, boolean isCypherplay, boolean needRefresh) {
        if (vpnServer != null) {
            vpnSetting.updateCypherplayEnabled(isCypherplay);
            vpnSetting.updateRegionId(vpnServer.id());
            onVpnServerChanged(vpnServer);
            if (needRefresh) {
                refreshRegionList();
            }
        }
    }

    private void onVpnServerChanged(@NonNull VpnServer vpnServer) {
        final int flagResId = RegionAdapter.getFlag(getContext(), vpnServer.country());

        nameView.setText(vpnServer.name());
        flagView.setImageResource(flagResId);
        tagView.setLevel(vpnServer.level());

        if (listener != null) {
            listener.onSelectedRegionChanged(vpnServer.name(), flagResId, false);
        }
    }
}
