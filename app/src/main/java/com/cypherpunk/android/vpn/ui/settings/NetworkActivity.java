package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.commonsware.cwac.merge.MergeAdapter;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityNetworkBinding;
import com.cypherpunk.android.vpn.databinding.ListItemWifiBinding;
import com.cypherpunk.android.vpn.model.CypherpunkSetting;

public class NetworkActivity extends AppCompatActivity {

    private ActivityNetworkBinding binding;
    private CypherpunkSetting cypherpunkSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_network);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_network);
        }
        cypherpunkSetting = new CypherpunkSetting();

        binding.list.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.divider)));
        binding.list.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.divider));
        MergeAdapter mergeAdapter = new MergeAdapter();
        mergeAdapter.addView(buildListHeader());

        ArrayAdapter<String> wifiAdapter = new WifiAdapter(this);
        String connectingSSID = getConnectedSSID();
        if (!TextUtils.isEmpty(connectingSSID)) {
            wifiAdapter.add(connectingSSID);
            mergeAdapter.addAdapter(wifiAdapter);
        }

        mergeAdapter.addView(buildListFooter());
        binding.list.setAdapter(mergeAdapter);
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

    @Nullable
    private String getConnectedSSID() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        return ssid.replace("\"", "");
    }

    private View buildListHeader() {
        final View header = LayoutInflater.from(this)
                .inflate(R.layout.list_item_header_neteork_secure, binding.list, false);
        final SwitchCompat autoSecureSwitch = (SwitchCompat) header.findViewById(R.id.auto_secure_switch);
        autoSecureSwitch.setChecked(cypherpunkSetting.autoSecureUntrusted);
        header.findViewById(R.id.auto_secure_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSecureSwitch.toggle();
                cypherpunkSetting.autoSecureUntrusted = autoSecureSwitch.isChecked();
                cypherpunkSetting.save();
            }
        });
        return header;
    }

    private View buildListFooter() {
        View footer = LayoutInflater.from(this)
                .inflate(R.layout.list_item_footer_network_other, binding.list, false);
        final SwitchCompat otherAutoSecureSwitch =
                (SwitchCompat) footer.findViewById(R.id.other_auto_secure_switch);
        otherAutoSecureSwitch.setChecked(cypherpunkSetting.autoSecureOther);
        footer.findViewById(R.id.other_auto_secure_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherAutoSecureSwitch.toggle();
                cypherpunkSetting.autoSecureOther = otherAutoSecureSwitch.isChecked();
                cypherpunkSetting.save();
            }
        });
        return footer;
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
