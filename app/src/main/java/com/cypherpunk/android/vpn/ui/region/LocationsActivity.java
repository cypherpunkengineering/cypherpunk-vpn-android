package com.cypherpunk.android.vpn.ui.region;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityLocationsBinding;
import com.cypherpunk.android.vpn.databinding.ListItemLocationBinding;
import com.cypherpunk.android.vpn.model.Location;

public class LocationsActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,
        ConnectConfirmationDialogFragment.ConnectDialogListener {

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
        cityAdapter.add(new Location("Tokyo 1", "208.111.52.1 7133", 305, 56));
        cityAdapter.add(new Location("Tokyo 2", "208.111.52.2 7133", 305, 56));
        cityAdapter.add(new Location("Honolulu", "199.68.252.203 7133", 355, 66));
        mergeAdapter.addAdapter(cityAdapter);

        // region
        mergeAdapter.addView(buildHeader(R.string.location_all_locations));
        ArrayAdapter<Location> regionAdapter = new LocationAdapter(this);
        mergeAdapter.addAdapter(regionAdapter);

        binding.list.setAdapter(mergeAdapter);
        binding.list.setDivider(null);
        binding.list.setOnItemClickListener(this);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        selectedItem = (Location) mergeAdapter.getItem(position);
        ConnectConfirmationDialogFragment dialogFragment = ConnectConfirmationDialogFragment.newInstance(selectedItem);
        dialogFragment.show(getSupportFragmentManager());
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String name = getItem(position).getName();
            ListItemLocationBinding binding;
            if (convertView == null) {
                binding = DataBindingUtil.inflate(inflater, R.layout.list_item_location, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemLocationBinding) convertView.getTag();
            }
            binding.location.setText(name);

            return convertView;
        }
    }
}
