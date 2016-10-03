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

public class LocationsActivity extends AppCompatActivity
        implements ConnectConfirmationDialogFragment.ConnectDialogListener {

    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_CONNECT = "connect";

    private ActivityLocationsBinding binding;
    private MergeAdapter mergeAdapter;
    private Location selectedItem;

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

        mergeAdapter = new MergeAdapter();

        // recently connected
        mergeAdapter.addView(buildHeader(R.string.location_recommended));
        ArrayAdapter<Location> cityAdapter = new LocationAdapter(this);
        cityAdapter.add(new Location("Tokyo Test", 305, 56, "208.111.52.34", "208.111.52.35", "208.111.52.36", "208.111.52.37"));
        cityAdapter.add(new Location("Tokyo 1", 305, 56, "208.111.52.1", "208.111.52.1", "208.111.52.1", "208.111.52.1"));
        cityAdapter.add(new Location("Tokyo 2", 305, 56, "208.111.52.2", "208.111.52.2", "208.111.52.2", "208.111.52.2"));
        cityAdapter.add(new Location("Honolulu 1", 355, 66, "199.68.252.203", "199.68.252.203", "199.68.252.203", "199.68.252.203"));
        mergeAdapter.addAdapter(cityAdapter);
        mergeAdapter.addView(buildHeader());

        // region
        mergeAdapter.addView(buildHeader(R.string.location_all_locations));
        ArrayAdapter<Location> regionAdapter = new LocationAdapter(this);
        regionAdapter.add(new Location("Tokyo Test", 305, 56, "208.111.52.34", "208.111.52.35", "208.111.52.36", "208.111.52.37"));
        regionAdapter.add(new Location("Tokyo 1", 305, 56, "208.111.52.1", "208.111.52.1", "208.111.52.1", "208.111.52.1"));
        regionAdapter.add(new Location("Tokyo 2", 305, 56, "208.111.52.2", "208.111.52.2", "208.111.52.2", "208.111.52.2"));
        regionAdapter.add(new Location("Honolulu 1", 355, 66, "199.68.252.203", "199.68.252.203", "199.68.252.203", "199.68.252.203"));

        mergeAdapter.addAdapter(regionAdapter);

        binding.list.setAdapter(mergeAdapter);
        binding.list.setDivider(null);
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
        intent.putExtra(EXTRA_LOCATION, selectedItem);
        intent.putExtra(EXTRA_CONNECT, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDialogNegativeButtonClick() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_LOCATION, selectedItem);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final Location item = getItem(position);
            ListItemLocationBinding binding;
            if (convertView == null) {
                binding = DataBindingUtil.inflate(inflater, R.layout.list_item_location, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemLocationBinding) convertView.getTag();
            }
            binding.location.setText(item.getName());
            binding.favorite.setChecked(item.isFavorite());
            binding.favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    item.setFavorite(b);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedItem = item;
                    ConnectConfirmationDialogFragment dialogFragment = ConnectConfirmationDialogFragment.newInstance(selectedItem);
                    dialogFragment.show(getSupportFragmentManager());
                }
            });

            return convertView;
        }
    }
}
