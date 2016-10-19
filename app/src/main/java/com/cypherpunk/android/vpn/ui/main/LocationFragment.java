package com.cypherpunk.android.vpn.ui.main;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import com.cypherpunk.android.vpn.data.api.json.LocationResult;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.databinding.FragmentLocationBinding;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;
import com.cypherpunk.android.vpn.model.FavoriteRegion;
import com.cypherpunk.android.vpn.model.Location;
import com.cypherpunk.android.vpn.ui.region.ConnectConfirmationDialogFragment;
import com.squareup.picasso.Picasso;

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


public class LocationFragment extends Fragment {

    private Realm realm;
    private FragmentLocationBinding binding;
    private Subscription subscription = Subscriptions.empty();
    private LocationAdapter adapter;

    @Inject
    CypherpunkService webService;
    private LocationFragmentListener listener;

    public interface LocationFragmentListener {
        void toggleBottomSheetState();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LocationFragmentListener) {
            listener = (LocationFragmentListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((CypherpunkApplication) getActivity().getApplication()).getAppComponent().inject(this);
        binding = DataBindingUtil.bind(getView());

        realm = Realm.getDefaultInstance();

        getServerList();
        CypherpunkSetting setting = new CypherpunkSetting();
        if (!TextUtils.isEmpty(setting.locationId)) {
            Location location = realm.where(Location.class).equalTo("id", setting.locationId).findFirst();
            binding.region.setText(location.getRegionName());
            Picasso.with(getActivity()).load(location.getNationalFlagUrl()).into(binding.nationalFlag);
        }

        adapter = new LocationAdapter(getLocation()) {
            @Override
            protected void onFavorite(final String locationId, final boolean favorite) {
                RealmResults<FavoriteRegion> result = realm.where(FavoriteRegion.class).equalTo("id", locationId).findAll();
                Location location = realm.where(Location.class).equalTo("id", locationId).findFirst();

                realm.beginTransaction();
                location.setFavorited(favorite);
                if (favorite) {
                    if (result.size() == 0) {
                        realm.copyToRealm(new FavoriteRegion(location.getId()));
                    }
                } else {
                    result.deleteAllFromRealm();
                }
                realm.commitTransaction();
            }

            @Override
            protected void onItemClick(final String locationId) {
                CypherpunkSetting setting = new CypherpunkSetting();
                if (setting.locationId.equals(locationId)) {
                    // 既に選択されている
                    return;
                }
                setting.locationId = locationId;
                setting.save();

                ConnectConfirmationDialogFragment dialogFragment =
                        ConnectConfirmationDialogFragment.newInstance(locationId);
                dialogFragment.show(getChildFragmentManager());

                Location location = realm.where(Location.class).equalTo("id", locationId).findFirst();
                binding.region.setText(location.getRegionName());
                Picasso.with(getActivity()).load(location.getNationalFlagUrl()).into(binding.nationalFlag);
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

    private ArrayList<Location> getLocation() {
        RealmResults<Location> locationList = realm.where(Location.class).findAll();
        return new ArrayList<>(locationList);
    }

    private void getServerList() {
        subscription = webService
                .login(new LoginRequest(UserManager.getMailAddress(), UserManager.getPassword()))
                .flatMap(new Func1<ResponseBody, Single<Map<String, Map<String, LocationResult[]>>>>() {
                    @Override
                    public Single<Map<String, Map<String, LocationResult[]>>> call(ResponseBody responseBody) {
                        return webService.serverList();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Map<String, Map<String, LocationResult[]>>>() {
                               @Override
                               public void onSuccess(Map<String, Map<String, LocationResult[]>> result) {
                                   List<Location> locations = new ArrayList<>();
                                   for (Map.Entry<String, Map<String, LocationResult[]>> area : result.entrySet()) {
                                       Set<Map.Entry<String, LocationResult[]>> areaSet = area.getValue().entrySet();
                                       for (Map.Entry<String, LocationResult[]> country : areaSet) {
                                           LocationResult[] regions = country.getValue();
                                           for (LocationResult region : regions) {
                                               Location location = new Location(
                                                       region.getId(),
                                                       country.getKey(),
                                                       region.getRegionName(),
                                                       region.getOvHostname(),
                                                       region.getOvDefault(),
                                                       region.getOvNone(),
                                                       region.getOvStrong(),
                                                       region.getOvStealth(),
                                                       "http://flags.fmcdn.net/data/flags/normal/" + country.getKey().toLowerCase() + ".png");
                                               long id = realm.where(FavoriteRegion.class)
                                                       .equalTo("id", location.getId()).count();
                                               if (id != 0) {
                                                   location.setFavorited(true);
                                               }
                                               locations.add(location);
                                           }
                                       }
                                   }
                                   realm.beginTransaction();
                                   RealmResults<Location> locationList = realm.where(Location.class).findAll();
                                   locationList.deleteAllFromRealm();

                                   realm.copyToRealm(locations);
                                   realm.commitTransaction();

                                   // TODO: 一番上のを選択している
                                   CypherpunkSetting setting = new CypherpunkSetting();
                                   if (TextUtils.isEmpty(setting.locationId)) {
                                       Location first = realm.where(Location.class).findFirst();
                                       setting.locationId = first.getId();
                                       setting.save();

                                       binding.region.setText(first.getRegionName());
                                       Picasso.with(getActivity()).load(first.getNationalFlagUrl()).into(binding.nationalFlag);
                                       adapter.addAll(getLocation());
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
