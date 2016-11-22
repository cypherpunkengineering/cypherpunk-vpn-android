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
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.LoginRequest;
import com.cypherpunk.privacy.data.api.json.LoginResult;
import com.cypherpunk.privacy.data.api.json.RegionResult;
import com.cypherpunk.privacy.databinding.FragmentRegionBinding;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.Region;
import com.cypherpunk.privacy.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class RegionFragment extends Fragment {

    private Realm realm;
    private FragmentRegionBinding binding;
    private Subscription subscription = Subscriptions.empty();
    private RegionAdapter adapter;
    private RealmChangeListener realmChangeListener;

    @Inject
    CypherpunkService webService;
    private RegionFragmentListener listener;

    public interface RegionFragmentListener {
        void toggleBottomSheetState();

        void onSelectedRegionChanged(@NonNull String regionName, @DrawableRes int nationalFlagResId);
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

        realm = CypherpunkApplication.instance.getAppComponent().getDefaultRealm();

        getServerList();
        CypherpunkSetting setting = new CypherpunkSetting();
        if (!TextUtils.isEmpty(setting.regionId)) {
            Region region = realm.where(Region.class).equalTo("id", setting.regionId).findFirst();
            if (region != null) {
                binding.regionName.setText(region.getRegionName());
                int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), region.getCountryCode().toLowerCase());
                binding.nationalFlag.setImageResource(nationalFlagResId);

                binding.progress.setVisibility(View.GONE);
            }
        }
        if (getResources().getBoolean(R.bool.is_tablet)) {
            binding.regionContainer.setVisibility(View.GONE);
        }

        adapter = new RegionAdapter() {
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
                CypherpunkSetting setting = new CypherpunkSetting();
                if (setting.regionId.equals(regionId)) {
                    // 既に選択されている
                    return;
                }
                selectRegion(realm.where(Region.class).equalTo("id", regionId).findFirst());
            }

            @Override
            protected void onFastestLocationClick() {
                //TODO: fastest location
                Region region = realm.where(Region.class).findFirst();
                CypherpunkSetting setting = new CypherpunkSetting();
                if (region.getId().equals(setting.regionId)) {
                    // 既に選択されている
                    return;
                }
                selectRegion(region);
            }
        };
        binding.list.setAdapter(adapter);
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object element) {
                adapter.addRegionList(getFavoriteRegionList(), getRecentlyConnectedList(), getOtherList());
            }
        };
        realm.addChangeListener(realmChangeListener);
        adapter.addRegionList(getFavoriteRegionList(), getRecentlyConnectedList(), getOtherList());

        binding.regionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toggleBottomSheetState();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.removeChangeListener(realmChangeListener);
        realm.close();
        subscription.unsubscribe();
    }

    public void toggleAllowIcon(boolean more) {
        binding.allow.setImageResource(more ? R.drawable.expand_more_vector : R.drawable.expand_less_vector);
    }

    public void refreshServerList() {
        getServerList();
    }

    private ArrayList<Region> getFavoriteRegionList() {
        RealmResults<Region> regionList = realm.where(Region.class).equalTo("favorited", true).findAllSorted("lastConnectedDate", Sort.DESCENDING);
        return new ArrayList<>(regionList);
    }

    private ArrayList<Region> getRecentlyConnectedList() {
        RealmResults<Region> regionList = realm.where(Region.class).notEqualTo("lastConnectedDate", new Date(0)).findAllSorted("lastConnectedDate", Sort.DESCENDING);
        ArrayList<Region> regions = new ArrayList<>(regionList);
        if (regionList.size() > 3) {
            regions = new ArrayList<>(regionList.subList(0, 3));
        }
        return regions;
    }

    private ArrayList<Region> getOtherList() {
        RealmResults<Region> regionList = realm.where(Region.class).findAll();
        return new ArrayList<>(regionList);
    }

    private void selectRegion(Region region) {
        CypherpunkSetting setting = new CypherpunkSetting();
        setting.regionId = region.getId();
        setting.save();

        binding.regionName.setText(region.getRegionName());
        int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), region.getCountryCode().toLowerCase());
        binding.nationalFlag.setImageResource(nationalFlagResId);
        adapter.addRegionList(getFavoriteRegionList(), getRecentlyConnectedList(), getOtherList());

        listener.onSelectedRegionChanged(region.getRegionName(), nationalFlagResId);
    }

    private void getServerList() {
        subscription = webService
                .login(new LoginRequest(UserManager.getMailAddress(), UserManager.getPassword()))
                .flatMap(new Func1<LoginResult, Single<Map<String, Map<String, RegionResult[]>>>>() {
                    @Override
                    public Single<Map<String, Map<String, RegionResult[]>>> call(LoginResult result) {
                        UserManager.saveSecret(result.getSecret());
                        return webService.serverList();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Map<String, Map<String, RegionResult[]>>>() {
                               @Override
                               public void onSuccess(Map<String, Map<String, RegionResult[]>> result) {
                                   realm.beginTransaction();
                                   List<String> updateRegionIdList = new ArrayList<>();
                                   for (Map.Entry<String, Map<String, RegionResult[]>> area : result.entrySet()) {
                                       Set<Map.Entry<String, RegionResult[]>> areaSet = area.getValue().entrySet();
                                       for (Map.Entry<String, RegionResult[]> country : areaSet) {
                                           RegionResult[] regions = country.getValue();
                                           for (RegionResult regionResult : regions) {
                                               Region region = realm.where(Region.class)
                                                       .equalTo("id", regionResult.getId()).findFirst();
                                               if (region != null) {
                                                   region.setRegionName(regionResult.getRegionName());
                                                   region.setOvHostname(regionResult.getOvHostname());
                                                   region.setOvDefault(regionResult.getOvDefault());
                                                   region.setOvNone(regionResult.getOvNone());
                                                   region.setOvStrong(regionResult.getOvStrong());
                                                   region.setOvStealth(regionResult.getOvStealth());
                                               } else {
                                                   region = new Region(
                                                           regionResult.getId(),
                                                           country.getKey(),
                                                           regionResult.getRegionName(),
                                                           regionResult.getOvHostname(),
                                                           regionResult.getOvDefault(),
                                                           regionResult.getOvNone(),
                                                           regionResult.getOvStrong(),
                                                           regionResult.getOvStealth());
                                                   realm.copyToRealm(region);
                                               }
                                               updateRegionIdList.add(region.getId());
                                           }
                                       }
                                   }
                                   RealmResults<Region> oldRegion = realm.where(Region.class)
                                           .not()
                                           .beginGroup()
                                           .in("id", updateRegionIdList.toArray(new String[updateRegionIdList.size()]))
                                           .endGroup()
                                           .findAll();
                                   oldRegion.deleteAllFromRealm();
                                   realm.commitTransaction();

                                   adapter.addRegionList(getFavoriteRegionList(), getRecentlyConnectedList(), getOtherList());

                                   // TODO: 一番上のを選択している
                                   CypherpunkSetting setting = new CypherpunkSetting();
                                   if (TextUtils.isEmpty(setting.regionId)) {
                                       Region first = realm.where(Region.class).findFirst();
                                       setting.regionId = first.getId();
                                       setting.save();

                                       binding.regionName.setText(first.getRegionName());
                                       int nationalFlagResId = ResourceUtil.getFlagDrawableByKey(getContext(), first.getCountryCode().toLowerCase());
                                       binding.nationalFlag.setImageResource(nationalFlagResId);
                                   }
                                   binding.progress.setVisibility(View.GONE);
                               }

                               @Override
                               public void onError(Throwable error) {
                                   error.printStackTrace();
                               }
                           }
                );


    }
}
