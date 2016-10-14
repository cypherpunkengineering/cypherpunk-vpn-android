package com.cypherpunk.android.vpn.ui.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.FragmentLocationBinding;
import com.cypherpunk.android.vpn.model.Location;
import com.cypherpunk.android.vpn.ui.region.ConnectConfirmationDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


public class LocationFragment extends Fragment {

    private Realm realm;
    private FragmentLocationBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding = DataBindingUtil.bind(getView());

        realm = Realm.getDefaultInstance();

        Location location = realm.where(Location.class).equalTo("selected", true).findFirst();
        if (location == null) {
            getServerList();
            location = realm.where(Location.class).equalTo("selected", true).findFirst();
        }

        binding.region.setText(location.getCity());
        Picasso.with(getActivity()).load(location.getNationalFlagUrl()).into(binding.nationalFlag);

        LocationAdapter adapter = new LocationAdapter(getLocation()) {
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
    }

    private ArrayList<Location> getLocation() {
        RealmResults<Location> locationList = realm.where(Location.class).findAll();
        return new ArrayList<>(locationList);
    }

    private void getServerList() {
        realm.beginTransaction();
        List<Location> locations = new ArrayList<>();
        // TODO: serverList api
        locations.add(new Location("Tokyo Dev", "JP", "freebsd-test.tokyo.vpn.cypherpunk.network", "208.111.52.34", "208.111.52.35", "208.111.52.36", "208.111.52.37", "http://flags.fmcdn.net/data/flags/normal/jp.png", 305, 56));
        locations.add(new Location("Tokyo", "JP", "freebsd2.tokyo.vpn.cypherpunk.network", "208.111.52.2", "208.111.52.12", "208.111.52.22", "208.111.52.32", "http://flags.fmcdn.net/data/flags/normal/jp.png", 305, 56));
        locations.add(new Location("Honolulu", "US", "honolulu.vpn.cypherpunk.network", "199.68.252.203", "199.68.252.203", "199.68.252.203", "199.68.252.203", "http://flags.fmcdn.net/data/flags/normal/us.png", 355, 66));
        realm.copyToRealm(locations);
        Location first = realm.where(Location.class).findFirst();
        first.setSelected(true);
        realm.commitTransaction();
    }
}
