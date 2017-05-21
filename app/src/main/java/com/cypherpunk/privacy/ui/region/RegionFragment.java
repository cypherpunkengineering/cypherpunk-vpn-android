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
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.CypherpunkService;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.databinding.FragmentRegionBinding;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.Region;
import com.cypherpunk.privacy.model.UserSetting;
import com.cypherpunk.privacy.utils.ResourceUtil;
import com.cypherpunk.privacy.vpn.ServerPingerThinger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class RegionFragment extends Fragment {

    private Realm realm;
    private FragmentRegionBinding binding;
    @NonNull
    private Disposable disposable = Disposables.empty();
    private RegionAdapter adapter;
    private RealmChangeListener realmChangeListener;

    @Inject
    NetworkRepository networkRepository;
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
        final VpnSetting vpnSetting = CypherpunkSetting.vpnSetting();

        realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();

        // if no location set, select fastest region using available data
        if (TextUtils.isEmpty(vpnSetting.regionId())) {
            Region first = ServerPingerThinger.getFastestLocation();
            if (first != null) {
                vpnSetting.updateRegionId(first.getId());
            }
        }

        if (!TextUtils.isEmpty(vpnSetting.regionId())) {
            Region region = realm.where(Region.class)
                    .equalTo("id", vpnSetting.regionId())
                    .findFirst();
            if (region != null) {
                int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), region.getCountry().toLowerCase());
                updateRegion(region);
                listener.onSelectedRegionChanged(region.getRegionName(), nationalFlagResId, false);

                binding.progress.setVisibility(View.GONE);
            }
        }
        if (getResources().getBoolean(R.bool.is_tablet)) {
            binding.regionContainer.setVisibility(View.GONE);
        }

        adapter = new RegionAdapter(getContext()) {
            @Override
            protected void onFavorite(@NonNull final String regionId, final boolean favorite) {
                Region region = realm.where(Region.class).equalTo("id", regionId).findFirst();
                if (favorite != region.isFavorited()) {
                    realm.beginTransaction();
                    region.setFavorited(favorite);
                    realm.commitTransaction();
                }
            }

            @Override
            protected void onItemClick(@NonNull final String regionId) {
                // disable cypherplay mode for ordinary locations
                final VpnSetting vpnSetting = CypherpunkSetting.vpnSetting();
                vpnSetting.updateCypherplayEnabled(false);

                // select region matching the selected id
                selectRegion(realm.where(Region.class).equalTo("id", regionId).findFirst());
            }

            @Override
            protected void onCypherplayClick() {
                Region region = ServerPingerThinger.getFastestLocation();
                if (region != null) {
                    final VpnSetting vpnSetting = CypherpunkSetting.vpnSetting();
                    vpnSetting.updateCypherplayEnabled(true);
                    selectRegion(region);
                }
            }

            @Override
            protected void onFastestLocationClick() {
                Region region = ServerPingerThinger.getFastestLocation();
                if (region != null) {
                    final VpnSetting vpnSetting = CypherpunkSetting.vpnSetting();
                    vpnSetting.updateCypherplayEnabled(false);
                    selectRegion(region);
                }
            }
        };
        binding.list.setAdapter(adapter);
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {
                refreshRegionList();
            }
        };
        realm.addChangeListener(realmChangeListener);
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
        realm.removeChangeListener(realmChangeListener);
        realm.close();
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
                getFavoriteRegionList(),
                getRecentlyConnectedList(),
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

    private ArrayList<Region> getFavoriteRegionList() {
        RealmResults<Region> regionList = realm.where(Region.class)
                .equalTo("favorited", true).findAllSorted("regionName", Sort.ASCENDING);
        return new ArrayList<>(regionList);
    }

    private ArrayList<Region> getRecentlyConnectedList() {
        RealmResults<Region> regionList = realm.where(Region.class)
                .equalTo("favorited", false)
                .notEqualTo("lastConnectedDate", new Date(0)).findAllSorted("lastConnectedDate", Sort.DESCENDING);
        ArrayList<Region> regions = new ArrayList<>(regionList);
        if (regionList.size() > 3) {
            regions = new ArrayList<>(regionList.subList(0, 3));
        }
        return regions;
    }

    private ArrayList<Region> getOtherList(String region) {
        ArrayList<Region> regionList = new ArrayList<>(
                realm.where(Region.class).equalTo("region", region).findAll());
        final Locale locale = Locale.getDefault();
        Collections.sort(regionList, new Comparator<Region>() {
            @Override
            public int compare(Region region1, Region region2) {
                Locale countryLocale1 = new Locale(locale.getLanguage(), region1.getCountry());
                String regionCountryName1 = countryLocale1.getDisplayCountry(locale);

                Locale countryLocale2 = new Locale(locale.getLanguage(), region2.getCountry());
                String region1CountryName2 = countryLocale2.getDisplayCountry(locale);
                int result = regionCountryName1.compareTo(region1CountryName2);
                if (result == 0) {
                    return region1.getRegionName().compareTo(region2.getRegionName());
                }
                return result;
            }
        });
        return regionList;
    }

    private void selectRegion(Region region) {
        final VpnSetting vpnSetting = CypherpunkSetting.vpnSetting();
        vpnSetting.updateRegionId(region.getId());

        int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), region.getCountry().toLowerCase());
        updateRegion(region);
        refreshRegionList();

        listener.onSelectedRegionChanged(region.getRegionName(), nationalFlagResId, true);
    }

    private void updateRegion(Region region) {
        binding.regionName.setText(region.getRegionName());
        int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), region.getCountry().toLowerCase());
        binding.nationalFlag.setImageResource(nationalFlagResId);
        binding.regionTag.setRegionLevel(region.getLevel());
    }

    private void getServerList() {
        disposable = networkRepository.getAccountStatus()
                .flatMap(new Function<StatusResult, SingleSource<Map<String, RegionResult>>>() {
                    @Override
                    public SingleSource<Map<String, RegionResult>> apply(StatusResult result) throws Exception {
                        UserSetting.instance().updateAccountType(result.account.type);
                        return networkRepository.serverList(result.account.type);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Map<String, RegionResult>>() {
                    @Override
                    public void onSuccess(Map<String, RegionResult> result) {
                        realm.beginTransaction();
                        List<String> updateRegionIdList = new ArrayList<>();
                        for (Map.Entry<String, RegionResult> resultEntry : result.entrySet()) {
                            RegionResult regionResult = resultEntry.getValue();
                            Region region = realm.where(Region.class)
                                    .equalTo("id", regionResult.id).findFirst();
                            if (region != null) {
                                region.setRegion(regionResult.region);
                                region.setCountry(regionResult.country);
                                region.setLevel(regionResult.level);
                                region.setRegionName(regionResult.name);
                                region.setAuthorized(regionResult.authorized);
                                region.setOvHostname(regionResult.ovHostname);
                                region.setOvDefault(regionResult.ovDefault);
                                region.setOvNone(regionResult.ovNone);
                                region.setOvStrong(regionResult.ovStrong);
                                region.setOvStealth(regionResult.ovStealth);
                            } else {
                                region = new Region(
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
                                        regionResult.ovStealth);
                                realm.copyToRealm(region);
                            }
                            updateRegionIdList.add(region.getId());
                        }
                        RealmResults<Region> oldRegion = realm.where(Region.class)
                                .not()
                                .beginGroup()
                                .in("id", updateRegionIdList.toArray(new String[updateRegionIdList.size()]))
                                .endGroup()
                                .findAll();
                        oldRegion.deleteAllFromRealm();
                        realm.commitTransaction();

                        refreshRegionList();

                        // after realm db is updated above, start pinging new location data
                        for (Map.Entry<String, RegionResult> resultEntry : result.entrySet()) {
                            RegionResult regionResult = resultEntry.getValue();
                            Region region = realm.where(Region.class)
                                    .equalTo("id", regionResult.id).findFirst();
                            ServerPingerThinger.pingLocation(region);
                        }

                        // check if previously selected region is still available
                        final VpnSetting vpnSetting = CypherpunkSetting.vpnSetting();
                        Region region = realm.where(Region.class)
                                .equalTo("id", vpnSetting.regionId())
                                .equalTo("authorized", true)
                                .contains("ovDefault", ".")
                                .findFirst();

                        // if not, find a new one
                        if (region == null) {
                            region = ServerPingerThinger.getFastestLocation();
                            if (region != null) {
                                vpnSetting.updateRegionId(region.getId());
                            }
                        }
                        int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), region.getCountry().toLowerCase());
                        updateRegion(region);
                        listener.onSelectedRegionChanged(region.getRegionName(), nationalFlagResId, false);

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
