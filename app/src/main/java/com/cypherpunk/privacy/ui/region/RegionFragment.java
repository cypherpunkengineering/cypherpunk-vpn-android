package com.cypherpunk.privacy.ui.region;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.FragmentRegionBinding;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.domain.model.vpn.VpnServer;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.VpnServerRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.utils.ResourceUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RegionFragment extends Fragment {

    private FragmentRegionBinding binding;
    @NonNull
    private Disposable disposable = Disposables.empty();
    private RegionAdapter adapter;
    private VpnServerRepository.ChangeListener realmChangeListener;

    @Inject
    VpnServerRepository vpnServerRepository;

    @Inject
    NetworkRepository networkRepository;

    @Inject
    VpnSetting vpnSetting;

    @Inject
    AccountSetting accountSetting;

    private RegionFragmentListener listener;

    public interface RegionFragmentListener {
        void toggleBottomSheetState();

        void onSelectedRegionChanged(@NonNull String regionName, @DrawableRes int nationalFlagResId, boolean connectNow);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegionFragmentListener) {
            listener = (RegionFragmentListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_region, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((CypherpunkApplication) getActivity().getApplication()).getAppComponent().inject(this);
        binding = DataBindingUtil.bind(getView());

        getServerList();

        // if no location set, select fastest region using available data
        if (TextUtils.isEmpty(vpnSetting.regionId())) {
            final VpnServer fastest = vpnServerRepository.fastest();
            if (fastest != null) {
                vpnSetting.updateRegionId(fastest.getId());
            }
        }

        if (!TextUtils.isEmpty(vpnSetting.regionId())) {
            final VpnServer vpnServer = vpnServerRepository.find(vpnSetting.regionId());
            if (vpnServer != null) {
                final int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), vpnServer.getCountry().toLowerCase());
                updateRegion(vpnServer);
                listener.onSelectedRegionChanged(vpnServer.getRegionName(), nationalFlagResId, false);

                binding.progress.setVisibility(View.GONE);
            }
        }
        if (getResources().getBoolean(R.bool.is_tablet)) {
            binding.regionContainer.setVisibility(View.GONE);
        }

        adapter = new RegionAdapter(getContext(), vpnSetting) {
            @Override
            protected void onFavorite(@NonNull final String regionId, final boolean favorite) {
                vpnServerRepository.updateFavorite(regionId, favorite);
            }

            @Override
            protected void onItemClick(@NonNull final String regionId) {
                // disable cypherplay mode for ordinary locations
                vpnSetting.updateCypherplayEnabled(false);

                // select region matching the selected id
                selectRegion(vpnServerRepository.find(regionId));
            }

            @Override
            protected void onCypherplayClick() {
                final VpnServer fastest = vpnServerRepository.fastest();
                if (fastest != null) {
                    vpnSetting.updateCypherplayEnabled(true);
                    selectRegion(fastest);
                }
            }

            @Override
            protected void onFastestLocationClick() {
                final VpnServer fastest = vpnServerRepository.fastest();
                if (fastest != null) {
                    vpnSetting.updateCypherplayEnabled(false);
                    selectRegion(fastest);
                }
            }
        };
        binding.list.setAdapter(adapter);
        realmChangeListener = new VpnServerRepository.ChangeListener() {
            @Override
            public void onChanged() {
                refreshRegionList();
            }
        };
        vpnServerRepository.addChangeListener(realmChangeListener);
        refreshRegionList();

        binding.regionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toggleBottomSheetState();
            }
        });
    }

    @Override
    public void onDestroyView() {
        vpnServerRepository.removeChangeListener(realmChangeListener);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    public void toggleAllowIcon(boolean more) {
        binding.allow.setImageResource(more ? R.drawable.expand_more_vector : R.drawable.expand_less_vector);
    }

    public void refreshServerList() {
        getServerList();
    }

    private void refreshRegionList() {
        adapter.addRegionList(
                vpnServerRepository.findFavorites(),
                vpnServerRepository.findRecent(),
                getOtherList("DEV"),
                getOtherList("NA"),
                getOtherList("SA"),
                getOtherList("CR"),
                getOtherList("EU"),
                getOtherList("ME"),
                getOtherList("AF"),
                getOtherList("AS"),
                getOtherList("OP")
        );
    }

    @NonNull
    private List<VpnServer> getOtherList(String region) {
        final List<VpnServer> serverList = vpnServerRepository.findAllByRegion(region);
        final Locale locale = Locale.getDefault();
        Collections.sort(serverList, new Comparator<VpnServer>() {
            @Override
            public int compare(VpnServer vpnServer1, VpnServer vpnServer2) {
                Locale countryLocale1 = new Locale(locale.getLanguage(), vpnServer1.getCountry());
                String regionCountryName1 = countryLocale1.getDisplayCountry(locale);

                Locale countryLocale2 = new Locale(locale.getLanguage(), vpnServer2.getCountry());
                String region1CountryName2 = countryLocale2.getDisplayCountry(locale);
                int result = regionCountryName1.compareTo(region1CountryName2);
                if (result == 0) {
                    return vpnServer1.getRegionName().compareTo(vpnServer2.getRegionName());
                }
                return result;
            }
        });
        return serverList;
    }

    private void selectRegion(VpnServer vpnServer) {
        vpnSetting.updateRegionId(vpnServer.getId());

        int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), vpnServer.getCountry().toLowerCase());
        updateRegion(vpnServer);
        refreshRegionList();

        listener.onSelectedRegionChanged(vpnServer.getRegionName(), nationalFlagResId, true);
    }

    private void updateRegion(@NonNull VpnServer vpnServer) {
        binding.regionName.setText(vpnServer.getRegionName());
        int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), vpnServer.getCountry().toLowerCase());
        binding.nationalFlag.setImageResource(nationalFlagResId);
        binding.regionTag.setRegionLevel(vpnServer.getLevel());
    }

    private void getServerList() {
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

                        refreshRegionList();

                        // check if previously selected region is still available
                        VpnServer vpnServer = vpnServerRepository.findAuthorizedDefault(vpnSetting.regionId());

                        // if not, find a new one
                        if (vpnServer == null) {
                            vpnServer = vpnServerRepository.fastest();
                            if (vpnServer != null) {
                                vpnSetting.updateRegionId(vpnServer.getId());
                            }
                        }

                        if (vpnServer != null) {
                            int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), vpnServer.getCountry().toLowerCase());
                            updateRegion(vpnServer);
                            listener.onSelectedRegionChanged(vpnServer.getRegionName(), nationalFlagResId, false);
                        }

                        binding.progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        binding.progress.setVisibility(View.GONE);
                    }
                });
    }
}
