package com.cypherpunk.android.vpn.ui.main;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.CypherpunkService;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.data.api.json.RegionResult;
import com.cypherpunk.android.vpn.databinding.FragmentRegionBinding;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.FavoriteRegion;
import com.cypherpunk.android.vpn.model.Region;
import com.cypherpunk.android.vpn.ui.region.ConnectConfirmationDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
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

    @Inject
    CypherpunkService webService;
    private RegionFragmentListener listener;

    public interface RegionFragmentListener {
        void toggleBottomSheetState();
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
                binding.nationalFlag.setImageResource(getFlagDrawableByKey(region.getCountryCode().toLowerCase()));
            }
        }

        adapter = new RegionAdapter(getRegionList()) {
            @Override
            protected void onFavorite(@NonNull final String regionId, final boolean favorite) {
                RealmResults<FavoriteRegion> result = realm.where(FavoriteRegion.class).equalTo("id", regionId).findAll();
                Region region = realm.where(Region.class).equalTo("id", regionId).findFirst();

                realm.beginTransaction();
                region.setFavorited(favorite);
                if (favorite) {
                    if (result.size() == 0) {
                        realm.copyToRealm(new FavoriteRegion(region.getId()));
                    }
                } else {
                    result.deleteAllFromRealm();
                }
                realm.commitTransaction();
            }

            @Override
            protected void onItemClick(@NonNull final String regionId) {
                CypherpunkSetting setting = new CypherpunkSetting();
                if (setting.regionId.equals(regionId)) {
                    // 既に選択されている
                    return;
                }
                setting.regionId = regionId;
                setting.save();

                ConnectConfirmationDialogFragment dialogFragment =
                        ConnectConfirmationDialogFragment.newInstance(regionId);
                dialogFragment.show(getChildFragmentManager());

                Region region = realm.where(Region.class).equalTo("id", regionId).findFirst();
                binding.regionName.setText(region.getRegionName());
                binding.nationalFlag.setImageResource(getFlagDrawableByKey(region.getCountryCode().toLowerCase()));
            }
        };
        binding.list.setAdapter(adapter);

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
        realm.close();
        subscription.unsubscribe();
    }

    public void toggleAllowIcon(boolean more) {
        binding.allow.setImageResource(more ? R.drawable.expand_more_vector : R.drawable.expand_less_vector);
    }

    private int getFlagDrawableByKey(String key) {
        String packageName = getContext().getPackageName();
        return getContext().getResources().getIdentifier("flag_" + key, "drawable", packageName);
    }

    private ArrayList<Region> getRegionList() {
        RealmResults<Region> regionList = realm.where(Region.class).findAll();
        return new ArrayList<>(regionList);
    }

    private void getServerList() {
        subscription = webService
                .login(new LoginRequest(UserManager.getMailAddress(), UserManager.getPassword()))
                .flatMap(new Func1<ResponseBody, Single<Map<String, Map<String, RegionResult[]>>>>() {
                    @Override
                    public Single<Map<String, Map<String, RegionResult[]>>> call(ResponseBody responseBody) {
                        return webService.serverList();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Map<String, Map<String, RegionResult[]>>>() {
                               @Override
                               public void onSuccess(Map<String, Map<String, RegionResult[]>> result) {
                                   List<Region> regionList = new ArrayList<>();
                                   for (Map.Entry<String, Map<String, RegionResult[]>> area : result.entrySet()) {
                                       Set<Map.Entry<String, RegionResult[]>> areaSet = area.getValue().entrySet();
                                       for (Map.Entry<String, RegionResult[]> country : areaSet) {
                                           RegionResult[] regions = country.getValue();
                                           for (RegionResult regionResult : regions) {
                                               Region region = new Region(
                                                       regionResult.getId(),
                                                       country.getKey(),
                                                       regionResult.getRegionName(),
                                                       regionResult.getOvHostname(),
                                                       regionResult.getOvDefault(),
                                                       regionResult.getOvNone(),
                                                       regionResult.getOvStrong(),
                                                       regionResult.getOvStealth());

                                               long id = realm.where(FavoriteRegion.class)
                                                       .equalTo("id", regionResult.getId()).count();
                                               if (id != 0) {
                                                   region.setFavorited(true);
                                               }
                                               regionList.add(region);
                                           }
                                       }
                                   }
                                   realm.beginTransaction();
                                   RealmResults<Region> oldRegionListResult = realm.where(Region.class).findAll();
                                   oldRegionListResult.deleteAllFromRealm();
                                   realm.copyToRealm(regionList);
                                   realm.commitTransaction();

                                   adapter.addAll(getRegionList());

                                   // TODO: 一番上のを選択している
                                   CypherpunkSetting setting = new CypherpunkSetting();
                                   if (TextUtils.isEmpty(setting.regionId)) {
                                       Region first = realm.where(Region.class).findFirst();
                                       setting.regionId = first.getId();
                                       setting.save();

                                       binding.regionName.setText(first.getRegionName());
                                       binding.nationalFlag.setImageResource(getFlagDrawableByKey(first.getCountryCode().toLowerCase()));
                                   }
                               }

                               @Override
                               public void onError(Throwable error) {
                                   error.printStackTrace();
                               }
                           }
                );


    }
}
