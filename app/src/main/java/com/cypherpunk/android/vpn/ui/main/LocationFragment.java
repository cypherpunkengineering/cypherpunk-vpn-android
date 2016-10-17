package com.cypherpunk.android.vpn.ui.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
        Location location = realm.where(Location.class).equalTo("selected", true).findFirst();
        if (location != null) {
            binding.region.setText(location.getCity());
            Picasso.with(getActivity()).load(location.getNationalFlagUrl()).into(binding.nationalFlag);
        }

        adapter = new LocationAdapter(getLocation()) {
            @Override
            protected void onFavorite(final Location location, final boolean favorite) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        location.setFavorited(favorite);
                    }
                });
            }

            @Override
            protected void onItemClick(final Location location) {
                Location selected = realm.where(Location.class).equalTo("selected", true).findFirst();
                if (selected.getId().equals(location.getId())) {
                    // 既に選択されている
                    return;
                }
                realm.beginTransaction();
                selected.setSelected(false);
                location.setSelected(true);
                realm.commitTransaction();

                ConnectConfirmationDialogFragment dialogFragment =
                        ConnectConfirmationDialogFragment.newInstance(location.getId());
                dialogFragment.show(getChildFragmentManager());

                binding.region.setText(location.getCity());
                Picasso.with(getActivity()).load(location.getNationalFlagUrl()).into(binding.nationalFlag);
            }
        };
        binding.list.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        subscription.unsubscribe();
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
                                           LocationResult[] cities = country.getValue();
                                           for (LocationResult city : cities) {
                                               // TODO: hostname, flag url
                                               locations.add(new Location(city.getCity(),
                                                       country.getKey(), "",
                                                       city.getIpDefault(),
                                                       city.getIpNone(),
                                                       city.getIpStrong(),
                                                       city.getIpStealth(),
                                                       "http://flags.fmcdn.net/data/flags/normal/jp.png"));
                                           }
                                       }
                                   }
                                   realm.beginTransaction();
                                   realm.copyToRealmOrUpdate(locations);
                                   Location first = realm.where(Location.class).findFirst();
                                   first.setSelected(true);
                                   realm.commitTransaction();

                                   binding.region.setText(first.getCity());
                                   Picasso.with(getActivity()).load(first.getNationalFlagUrl()).into(binding.nationalFlag);
                                   adapter.addAll(getLocation());
                               }

                               @Override
                               public void onError(Throwable error) {
                                   error.printStackTrace();
                               }
                           }
                );


    }
}
