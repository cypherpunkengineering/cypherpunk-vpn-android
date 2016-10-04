package com.cypherpunk.android.vpn.ui.region;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityLocationsBinding;
import com.cypherpunk.android.vpn.databinding.ListItemLocationBinding;
import com.cypherpunk.android.vpn.model.Location;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class LocationsActivity extends AppCompatActivity
        implements ConnectConfirmationDialogFragment.ConnectDialogListener {

    public static final String EXTRA_LOCATION_ID = "location_id";
    public static final String EXTRA_CONNECT = "connect";

    private ActivityLocationsBinding binding;
    private String selectedItemId;
    private Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.region_anim_in, R.anim.region_no_anim);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_locations);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_locations);
        }

        realm = Realm.getDefaultInstance();

        MergeAdapter mergeAdapter = new MergeAdapter();

        // favorite
        ArrayList<Location> favoriteLocation = getFavoriteLocation();
        if (!favoriteLocation.isEmpty()) {
            mergeAdapter.addView(buildHeader(R.string.location_favorite));
            ArrayAdapter<Location> favoriteAdapter = new LocationAdapter(this);
            favoriteAdapter.addAll(favoriteLocation);
            mergeAdapter.addAdapter(favoriteAdapter);
            mergeAdapter.addView(buildHeader());
        }

        // recommended
//        mergeAdapter.addView(buildHeader(R.string.location_recommended));
//        ArrayAdapter<Location> cityAdapter = new LocationAdapter(this);
//        // TODO:
//        mergeAdapter.addAdapter(cityAdapter);
//        mergeAdapter.addView(buildHeader());

        // all
        mergeAdapter.addView(buildHeader(R.string.location_all_locations));
        ArrayAdapter<Location> regionAdapter = new LocationAdapter(this);
        regionAdapter.addAll(getLocation());
        mergeAdapter.addAdapter(regionAdapter);

        binding.list.setAdapter(mergeAdapter);
        binding.list.setDivider(null);
    }

    private ArrayList<Location> getLocation() {
        realm.beginTransaction();
        RealmResults<Location> locationList = realm.where(Location.class).findAll();
        realm.commitTransaction();
        return new ArrayList<>(locationList);
    }

    private ArrayList<Location> getFavoriteLocation() {
        realm.beginTransaction();
        RealmResults<Location> favoritedList = realm.where(Location.class).equalTo("favorited", true).findAll();
        realm.commitTransaction();
        return new ArrayList<>(favoritedList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.region_anim_out);
    }

    private View buildHeader(@StringRes int text) {
        TextView textView = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.list_item_header_region_select, binding.list, false);
        textView.setText(text);
        return textView;
    }

    private View buildHeader() {
        View view = new View(this);
        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT,
                getResources().getDimensionPixelSize(R.dimen.list_divider)));
        return view;
    }

    @Override
    public void onDialogPositiveButtonClick() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_LOCATION_ID, selectedItemId);
        intent.putExtra(EXTRA_CONNECT, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDialogNegativeButtonClick() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_LOCATION_ID, selectedItemId);
        setResult(RESULT_OK, intent);
        finish();
    }

    private class LocationAdapter extends ArrayAdapter<Location> {

        private LayoutInflater inflater;

        public LocationAdapter(Context context) {
            super(context, 0);
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final Location item = getItem(position);
            ListItemLocationBinding binding;
            if (convertView == null) {
                binding = DataBindingUtil.inflate(inflater, R.layout.list_item_location, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemLocationBinding) convertView.getTag();
            }
            binding.location.setText(item.getCity());
            binding.favorite.setChecked(item.isFavorited());
            binding.favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            item.setFavorited(b);
                        }
                    });
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Location selected = realm.where(Location.class).equalTo("selected", true).findFirst();
                            selected.setSelected(false);
                            item.setSelected(true);
                        }
                    });
                    selectedItemId = item.getId();
                    ConnectConfirmationDialogFragment dialogFragment = ConnectConfirmationDialogFragment.newInstance(selectedItemId);
                    dialogFragment.show(getSupportFragmentManager());
                }
            });
            Picasso.with(getContext()).load(item.getNationalFlagUrl()).into(binding.nationalFlag);
            return convertView;
        }
    }
}
