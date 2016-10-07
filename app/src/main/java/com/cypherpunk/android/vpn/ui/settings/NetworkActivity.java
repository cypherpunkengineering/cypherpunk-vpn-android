package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityNetworkBinding;
import com.cypherpunk.android.vpn.databinding.ListItemWifiBinding;

public class NetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityNetworkBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_network);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_network);
        }

        MergeAdapter mergeAdapter = new MergeAdapter();
        View header = LayoutInflater.from(this)
                .inflate(R.layout.list_item_header_neteork_secure, binding.list, false);
        mergeAdapter.addView(header);

        ArrayAdapter<String> wifiAdapter = new WifiAdapter(this);
        wifiAdapter.add("SSID");
        wifiAdapter.add("SSID");
        wifiAdapter.add("SSID");
        mergeAdapter.addAdapter(wifiAdapter);

        View footer = LayoutInflater.from(this)
                .inflate(R.layout.list_item_footer_network_other, binding.list, false);
        mergeAdapter.addView(footer);

        binding.list.setAdapter(mergeAdapter);
        binding.list.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.divider)));
        binding.list.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.divider));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class WifiAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;

        WifiAdapter(Context context) {
            super(context, 0);
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ListItemWifiBinding binding;
            if (convertView == null) {
                binding = DataBindingUtil.inflate(inflater, R.layout.list_item_wifi, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemWifiBinding) convertView.getTag();
            }
            binding.networkItem.setText(getItem(position));


            return convertView;
        }
    }
}
